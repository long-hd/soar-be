package com.hdl.soar.module.pay.service.order;

import cn.hutool.extra.spring.SpringUtil;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.framework.tenant.core.util.TenantUtils;
import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hdl.soar.module.pay.controller.admin.order.dto.PayOrderPageReqDTO;
import com.hdl.soar.module.pay.dal.entity.app.PayAppPO;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderExtensionPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO_;
import com.hdl.soar.module.pay.dal.postgres.order.PayOrderExtensionRepository;
import com.hdl.soar.module.pay.dal.postgres.order.PayOrderRepository;
import com.hdl.soar.module.pay.dal.redis.no.PayNoRedisDAO;
import com.hdl.soar.module.pay.enums.PayCurrencyEnum;
import com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderChannelRespDTO;
import com.hdl.soar.module.pay.mapper.order.PayOrderMapper;
import com.hdl.soar.module.pay.service.app.PayAppService;
import com.hdl.soar.module.pay.service.channel.PayChannelService;
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

    /** Prefix for generated payment numbers. */
    static final String ORDER_NO_PREFIX = "P";

    PayOrderRepository orderRepository;
    PayOrderExtensionRepository orderExtensionRepository;
    PayNoRedisDAO noRedisDAO;

    PayAppService appService;
    PayChannelService channelService;

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
    public PayOrderExtensionPO createOrderExtension(Long orderId, String channelCode, String userIp) {
        // 1. Validate the order can still be paid
        PayOrderPO order = validateOrderCanSubmit(orderId);
        // 2. Validate the channel is enabled for this app
        PayChannelPO channel = channelService.validChannel(order.getAppId(), channelCode);

        // 3. Insert a WAITING extension with a fresh no
        String no = noRedisDAO.generate(ORDER_NO_PREFIX);
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
        // slice 3: enqueue an outbox notify task here (PayNotifyService.createPayNotifyTask)
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

    // ================ helper ================

    /** Resolve the Spring-proxied self, so calls to {@code @Transactional} methods go through the proxy. */
    private PayOrderServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
