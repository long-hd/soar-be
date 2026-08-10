package com.hdl.soar.module.pay.service.order;

import cn.hutool.extra.spring.SpringUtil;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.framework.tenant.core.util.TenantUtils;
import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hdl.soar.module.pay.controller.admin.order.dto.PayOrderPageReqDTO;
import com.hdl.soar.module.pay.controller.app.order.dto.PayOrderSubmitReqDTO;
import com.hdl.soar.module.pay.controller.app.order.dto.PayOrderSubmitRespDTO;
import com.hdl.soar.module.pay.dal.entity.app.PayAppPO;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderExtensionPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO_;
import com.hdl.soar.module.pay.dal.postgres.order.PayOrderExtensionRepository;
import com.hdl.soar.module.pay.dal.postgres.order.PayOrderRepository;
import com.hdl.soar.module.pay.dal.redis.no.PayNoRedisDAO;
import com.hdl.soar.module.pay.enums.PayChannelEnum;
import com.hdl.soar.module.pay.enums.PayCurrencyEnum;
import com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum;
import com.hdl.soar.module.pay.framework.pay.config.PayProperties;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClient;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.PayOrderGetReqDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderChannelRespDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import com.hdl.soar.module.pay.mapper.order.PayOrderMapper;
import com.hdl.soar.module.pay.service.app.PayAppService;
import com.hdl.soar.module.pay.service.channel.PayChannelService;
import com.hdl.soar.module.pay.service.notify.PayNotifyService;
import com.hdl.soar.module.pay.util.PayMoneyUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.eqIfPresent;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.likeIfPresent;
import static com.hdl.soar.module.pay.enums.ErrorCodeConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOrderServiceImpl implements PayOrderService {

    PayProperties payProperties;

    PayOrderRepository orderRepository;
    PayOrderExtensionRepository orderExtensionRepository;
    PayNoRedisDAO noRedisDAO;

    PayAppService appService;
    PayChannelService channelService;

    PayNotifyService payNotifyService;

    // ================ create ================

    @Override
    public Long createOrder(PayOrderCreateReqDTO reqDTO) {
        // 1. Validate app and currency
        PayAppPO app = appService.validApp(reqDTO.getAppKey());
        if (!PayCurrencyEnum.exists(reqDTO.getCurrency())) {
            throw exception(ORDER_CURRENCY_INVALID);
        }

        // 2. Idempotency: return the existing order for the same (app, merchant order id)
        var existing = orderRepository.findByAppIdAndMerchantOrderId(app.getId(), reqDTO.getMerchantOrderId());
        if (existing.isPresent()) {
            log.warn("[createOrder] order already exists for appId({}) merchantOrderId({})",
                    app.getId(), reqDTO.getMerchantOrderId());
            return existing.get().getId();
        }

        // 3. Insert a WAITING order
        PayOrderPO order = PayOrderMapper.INSTANCE.toPO(reqDTO);
        order.setAppId(app.getId());
        order.setNotifyUrl(app.getOrderNotifyUrl());
        order.setStatus(PayOrderStatusEnum.WAITING);
        order.setRefundPrice(BigDecimal.ZERO);
        orderRepository.save(order);
        return order.getId();
    }

    // ================ submit (channel-independent half) ================

    @Override
    public PayOrderSubmitRespDTO submitOrder(PayOrderSubmitReqDTO reqDTO, String userIp) {
        // 1. Validate EVERYTHING before creating any row — submitOrder is not transactional,
        //    so a validation failure must not leave an orphan WAITING extension behind.
        PayOrderPO order = validateOrderCanSubmit(reqDTO.getId());
        PayChannelPO channel = channelService.validChannel(order.getAppId(), reqDTO.getChannelCode());
        validateCurrencySupported(channel.getCode(), order.getCurrency());

        // 2. Now create the WAITING attempt
        PayOrderExtensionPO extension = createOrderExtension(order, channel, userIp);

        // 3. Call the rail
        PayClient<?> client = channelService.getPayClient(channel.getId());
        PayOrderUnifiedReqDTO unifiedReq = buildUnifiedReq(order, extension, channel.getId(), reqDTO, userIp);
        PayOrderChannelRespDTO channelResp = client.unifiedOrder(unifiedReq);

        // 4. If the rail returned a terminal result now (e.g. mock SUCCESS), drive the state machine.
        //    For async rails this is WAITING and notifyOrder is a no-op; SUCCESS arrives later via callback.
        if (channelResp != null) {
            notifyOrder(channel.getId(), channelResp);
        }

        // 5. Build the response from the (possibly updated) order
        order = getOrder(order.getId());
        PayOrderSubmitRespDTO resp = new PayOrderSubmitRespDTO();
        resp.setStatus(order.getStatus().getStatus());
        if (channelResp != null) {
            resp.setDisplayMode(channelResp.getDisplayMode());
            resp.setDisplayContent(channelResp.getDisplayContent());
        }
        return resp;
    }

    private PayOrderExtensionPO createOrderExtension(PayOrderPO order, PayChannelPO channel, String userIp) {
        String no = noRedisDAO.generate(payProperties.getOrderNoPrefix());
        PayOrderExtensionPO extension = PayOrderExtensionPO.builder()
                .no(no)
                .orderId(order.getId())
                .channelId(channel.getId())
                .channelCode(channel.getCode())
                .userIp(userIp)
                .status(PayOrderStatusEnum.WAITING)
                .build();
        orderExtensionRepository.save(extension);
        return extension;
    }

    private PayOrderPO validateOrderCanSubmit(Long id) {
        PayOrderPO order = getOrder(id);
        if (PayOrderStatusEnum.SUCCESS.equals(order.getStatus())) {
            throw exception(ORDER_STATUS_IS_SUCCESS);
        }
        if (!PayOrderStatusEnum.WAITING.equals(order.getStatus())) {
            throw exception(ORDER_STATUS_IS_NOT_WAITING);
        }
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(Instant.now())) {
            throw exception(ORDER_IS_EXPIRED);
        }
        // Guard against a prior attempt that already succeeded without a callback (db-only check;
        // the channel-side re-query is added in slice 2 together with PayClient)
        boolean anyPaid = orderExtensionRepository.findAllByOrderId(id).stream()
                .anyMatch(e -> PayOrderStatusEnum.SUCCESS.equals(e.getStatus()));
        if (anyPaid) {
            throw exception(ORDER_EXTENSION_IS_PAID);
        }
        return order;
    }

    private void validateCurrencySupported(String channelCode, PayCurrencyEnum currency) {
        PayChannelEnum rail = PayChannelEnum.of(channelCode);
        if (rail == null || !rail.getSupportedCurrencies().contains(currency)) {
            throw exception(ORDER_CURRENCY_INVALID);
        }
    }

    private PayOrderUnifiedReqDTO buildUnifiedReq(PayOrderPO order, PayOrderExtensionPO extension,
                                                  Long channelId, PayOrderSubmitReqDTO reqDTO, String userIp) {
        PayOrderUnifiedReqDTO req = new PayOrderUnifiedReqDTO();
        req.setUserIp(userIp);
        req.setOutTradeNo(extension.getNo());
        req.setSubject(order.getSubject());
        req.setBody(order.getBody());
        req.setPrice(order.getPrice());
        req.setCurrency(order.getCurrency());
        req.setNotifyUrl(payProperties.getOrderNotifyUrl() + "/" + channelId);
        req.setReturnUrl(reqDTO.getReturnUrl());
        req.setExpireTime(order.getExpireTime());
        req.setChannelExtras(reqDTO.getChannelExtras());
        req.setCreateTime(extension.getCreateTime());
        return req;
    }

    // ================ notify (state machine) ================

    @Override
    public void notifyOrder(Long channelId, PayOrderChannelRespDTO notify) {
        // Callbacks arrive without tenant context; load the channel ignoring tenant to read its
        // tenant id, then run the transactional handler inside that tenant's context.
        PayChannelPO channel = TenantUtils.executeIgnore(() -> channelService.validChannel(channelId));
        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyOrderInTransaction(channel, notify));
    }

    /**
     * Transactional notify handler. Called through {@code self} so the proxy applies the transaction;
     * a direct in-class call would bypass it.
     */
    @Transactional(rollbackFor = Exception.class)
    public void notifyOrderInTransaction(PayChannelPO channel, PayOrderChannelRespDTO notify) {
        if (PayOrderStatusEnum.isSuccess(notify.getStatus())) {
            notifyOrderSuccess(channel, notify);
            return;
        }
        if (PayOrderStatusEnum.isClosed(notify.getStatus())) {
            notifyOrderClosed(notify);
        }
        // WAITING: nothing to do. REFUND: handled by the refund flow (later slice).
    }

    private void notifyOrderSuccess(PayChannelPO channel, PayOrderChannelRespDTO notify) {
        PayOrderExtensionPO extension = updateOrderExtensionSuccess(notify);
        boolean alreadyPaid = updateOrderSuccess(channel, extension, notify);
        if (alreadyPaid) {
            return;
        }

        // slice 3: enqueue the outbox notify task in THIS transaction (transactional outbox).
        // The order is reloaded (not re-fetched from the channel) to hand PayNotifyService the
        // appId / notifyUrl / merchantOrderId without giving it a dependency on PayOrderService.
        PayOrderPO order = orderRepository.findById(extension.getOrderId())
                .orElseThrow(() -> exception(ORDER_NOT_FOUND));
        payNotifyService.createPayNotifyTask(order);
    }

    /**
     * Compare-and-swap the extension to SUCCESS. Returns the extension whether it was just updated or
     * had already succeeded (idempotent).
     */
    private PayOrderExtensionPO updateOrderExtensionSuccess(PayOrderChannelRespDTO notify) {
        PayOrderExtensionPO extension = orderExtensionRepository.findByNo(notify.getOutTradeNo())
                .orElseThrow(() -> exception(ORDER_EXTENSION_NOT_FOUND));
        if (PayOrderStatusEnum.SUCCESS.equals(extension.getStatus())) {
            return extension; // already paid; nothing to update
        }
        if (!PayOrderStatusEnum.WAITING.equals(extension.getStatus())) {
            throw exception(ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        int updated = orderExtensionRepository.updateStatusToSuccess(
                extension.getId(), PayOrderStatusEnum.WAITING, PayOrderStatusEnum.SUCCESS,
                JsonUtils.toJsonString(notify));
        if (updated == 0) {
            throw exception(ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        return extension;
    }

    /**
     * Compare-and-swap the order to SUCCESS.
     *
     * @return {@code true} if the order had already been paid by this same extension (duplicate
     *         callback), {@code false} if this call performed the transition
     */
    private boolean updateOrderSuccess(PayChannelPO channel, PayOrderExtensionPO extension, PayOrderChannelRespDTO notify) {
        PayOrderPO order = orderRepository.findById(extension.getOrderId())
                .orElseThrow(() -> exception(ORDER_NOT_FOUND));
        if (PayOrderStatusEnum.SUCCESS.equals(order.getStatus())
                && Objects.equals(order.getExtensionId(), extension.getId())) {
            return true; // already paid by this extension
        }
        if (!PayOrderStatusEnum.WAITING.equals(order.getStatus())) {
            throw exception(ORDER_STATUS_IS_NOT_WAITING);
        }
        BigDecimal feePrice = PayMoneyUtils.calculateFeePrice(order.getPrice(), channel.getFeeRate());
        int updated = orderRepository.updateStatusToSuccess(
                order.getId(), PayOrderStatusEnum.WAITING, PayOrderStatusEnum.SUCCESS,
                channel.getId(), channel.getCode(), notify.getSuccessTime(),
                extension.getId(), extension.getNo(), notify.getChannelOrderNo(), notify.getChannelUserId(),
                channel.getFeeRate(), feePrice);
        if (updated == 0) {
            throw exception(ORDER_STATUS_IS_NOT_WAITING);
        }
        return false;
    }

    private void notifyOrderClosed(PayOrderChannelRespDTO notify) {
        PayOrderExtensionPO extension = orderExtensionRepository.findByNo(notify.getOutTradeNo())
                .orElseThrow(() -> exception(ORDER_EXTENSION_NOT_FOUND));
        if (PayOrderStatusEnum.CLOSED.equals(extension.getStatus())) {
            return; // already closed
        }
        if (PayOrderStatusEnum.SUCCESS.equals(extension.getStatus())) {
            // Paid then closed is a full-refund scenario, handled by the refund flow, not here.
            return;
        }
        if (!PayOrderStatusEnum.WAITING.equals(extension.getStatus())) {
            throw exception(ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
        int updated = orderExtensionRepository.updateStatusToClosed(
                extension.getId(), PayOrderStatusEnum.WAITING, PayOrderStatusEnum.CLOSED,
                JsonUtils.toJsonString(notify), notify.getChannelErrorCode(), notify.getChannelErrorMsg());
        if (updated == 0) {
            throw exception(ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
        }
    }

    // ================ query ================

    @Override
    public PayOrderPO getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> exception(ORDER_NOT_FOUND));
    }

    @Override
    public PageResult<PayOrderPO> getOrderPage(PayOrderPageReqDTO pageReqDTO) {
        Specification<PayOrderPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, PayOrderPO_.appId, pageReqDTO.getAppId());
            eqIfPresent(predicates, cb, root, PayOrderPO_.channelCode, pageReqDTO.getChannelCode());
            likeIfPresent(predicates, cb, root, PayOrderPO_.merchantOrderId, pageReqDTO.getMerchantOrderId());
            likeIfPresent(predicates, cb, root, PayOrderPO_.no, pageReqDTO.getNo());
            eqIfPresent(predicates, cb, root, PayOrderPO_.status, PayOrderStatusEnum.of(pageReqDTO.getStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageUtils.toPageable(pageReqDTO);
        Page<PayOrderPO> page = orderRepository.findAll(spec, pageable);
        return PageUtils.toPageResult(page);    }

    @Override
    public PayOrderExtensionPO getOrderExtensionByNo(String no) {
        return orderExtensionRepository.findByNo(no)
                .orElseThrow(() -> exception(ORDER_EXTENSION_NOT_FOUND));
    }

    // ================ reconcile (sync) ================

    @Override
    public int syncOrder() {
        Instant cutoff = Instant.now().minus(payProperties.getOrderSyncCreateTimeWithin());
        List<PayOrderExtensionPO> extensions = orderExtensionRepository
                .findTop200ByStatusAndCreateTimeGreaterThanEqualOrderByIdAsc(
                        PayOrderStatusEnum.WAITING, cutoff);
        int count = 0;
        for (PayOrderExtensionPO extension : extensions) {
            if (syncOrder0(extension)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Reconcile one attempt. Loads the channel ignoring tenant (like {@link #notifyOrder}), asks the
     * channel for the transaction result, and on SUCCESS drives the order through {@code notifyOrder}
     * (CAS + outbox — idempotent against a real callback that may have raced). Returns whether it
     * recovered. A failure on one attempt is logged and swallowed so the batch continues.
     */
    private boolean syncOrder0(PayOrderExtensionPO extension) {
        try {
            PayClient<?> client = TenantUtils.executeIgnore(
                    () -> channelService.getPayClient(extension.getChannelId()));

            PayOrderGetReqDTO getReqDTO = new PayOrderGetReqDTO();
            getReqDTO.setOutTradeNo(extension.getNo());
            getReqDTO.setCreateTime(extension.getCreateTime());
            PayOrderChannelRespDTO resp = client.getOrder(getReqDTO);

            if (PayOrderStatusEnum.isSuccess(resp.getStatus())) {
                notifyOrder(extension.getChannelId(), resp);
                return true;
            }
            // CLOSED / still WAITING at the channel: leave it — a callback may still arrive, and the
            // expire job will close it if it's truly abandoned. Sync only advances the SUCCESS case.
            return false;
        } catch (Throwable ex) {
            log.warn("[syncOrder0][extension({}) no({}) reconcile failed]",
                    extension.getId(), extension.getNo(), ex);
            return false;
        }
    }

    // ================ expire ================

    @Override
    public int expireOrder() {
        List<PayOrderPO> orders = orderRepository
                .findTop200ByStatusAndExpireTimeLessThanOrderByIdAsc(
                        PayOrderStatusEnum.WAITING, Instant.now());
        int count = 0;
        for (PayOrderPO order : orders) {
            if (expireOrder0(order)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Expire one order. Before closing, re-checks every WAITING attempt against the channel one last
     * time: if any is actually paid, recover it via {@code notifyOrder} instead of closing (a dropped
     * callback for an order now past its expire window). Only if none recovered does it CAS-close the
     * order. Returns whether it closed. Per-order failures are logged and swallowed.
     */
    private boolean expireOrder0(PayOrderPO order) {
        try {
            List<PayOrderExtensionPO> extensions = orderExtensionRepository.findByOrderId(order.getId());
            for (PayOrderExtensionPO extension : extensions) {
                if (!PayOrderStatusEnum.WAITING.equals(extension.getStatus())) {
                    continue;
                }
                PayClient<?> client = TenantUtils.executeIgnore(
                        () -> channelService.getPayClient(extension.getChannelId()));

                PayOrderGetReqDTO getReqDTO = new PayOrderGetReqDTO();
                getReqDTO.setOutTradeNo(extension.getNo());
                getReqDTO.setCreateTime(extension.getCreateTime());
                PayOrderChannelRespDTO resp = client.getOrder(getReqDTO);

                if (PayOrderStatusEnum.isSuccess(resp.getStatus())) {
                    notifyOrder(extension.getChannelId(), resp); // recover, do not close
                    return false;
                }
            }
            return getSelf().closeExpiredOrder(order.getId());
        } catch (Throwable ex) {
            log.warn("[expireOrder0][order({}) expire failed]", order.getId(), ex);
            return false;
        }
    }

    /**
     * CAS-close a WAITING order that has expired. Through {@code getSelf()} so the transaction applies.
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean closeExpiredOrder(Long orderId) {
        int updated = orderRepository.updateStatusToClosed(
                orderId, PayOrderStatusEnum.WAITING, PayOrderStatusEnum.CLOSED);
        return updated > 0;
    }

    // ================ helper ================

    /** Resolve the Spring-proxied self, so calls to {@code @Transactional} methods go through the proxy. */
    private PayOrderServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
