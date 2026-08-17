package com.hdl.soar.module.pay.service.refund;

import cn.hutool.extra.spring.SpringUtil;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.framework.tenant.core.util.TenantUtils;
import com.hdl.soar.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import com.hdl.soar.module.pay.controller.admin.refund.dto.PayRefundPageReqDTO;
import com.hdl.soar.module.pay.dal.entity.app.PayAppPO;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.dal.entity.refund.PayRefundPO;
import com.hdl.soar.module.pay.dal.entity.refund.PayRefundPO_;
import com.hdl.soar.module.pay.dal.postgres.refund.PayRefundRepository;
import com.hdl.soar.module.pay.dal.redis.no.PayNoRedisDAO;
import com.hdl.soar.module.pay.enums.PayRefundStatusEnum;
import com.hdl.soar.module.pay.framework.pay.config.PayProperties;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClient;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.refund.PayRefundChannelRespDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.refund.PayRefundGetReqDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import com.hdl.soar.module.pay.service.app.PayAppService;
import com.hdl.soar.module.pay.service.channel.PayChannelService;
import com.hdl.soar.module.pay.service.notify.PayNotifyService;
import com.hdl.soar.module.pay.service.order.PayOrderService;
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
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;
import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.pay.enums.ErrorCodeConstants.*;

/**
 * Refund service. Reuses the order module's reliability skeleton: validate-then-insert, atomic CAS
 * transitions, the same {@code notifyRefund} entry for the inline result and the reconcile path
 * (idempotent for free), and the transactional outbox for merchant notification.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayRefundServiceImpl implements PayRefundService {

    PayProperties payProperties;
    PayRefundRepository refundRepository;
    PayNoRedisDAO noRedisDAO;

    PayAppService appService;
    PayChannelService channelService;
    PayOrderService orderService;
    PayNotifyService notifyService;

    @Override
    public Long createRefund(PayRefundCreateReqDTO reqDTO) {
        // 1. Validate app, order, channel, and refund uniqueness — ALL before inserting anything.
        PayAppPO app = appService.validApp(reqDTO.getAppKey());
        PayOrderPO order = validateOrderCanRefund(app.getId(), reqDTO);
        // Callbacks/in-process callers may not hold the channel's tenant; load it ignoring tenant.
        PayChannelPO channel = TenantUtils.executeIgnore(
                () -> channelService.validChannel(order.getChannelId()));
        if (refundRepository.findByAppIdAndMerchantRefundId(
                app.getId(), reqDTO.getMerchantRefundId()).isPresent()) {
            throw exception(REFUND_EXISTS);
        }

        // 2. Insert the refund as WAITING (global row, no tenant needed).
        String no = noRedisDAO.generate(payProperties.getRefundNoPrefix());
        String notifyUrl = app.getRefundNotifyUrl(); // may be null: merchant opted out of refund notify
        PayRefundPO refund = PayRefundPO.builder()
                .no(no).appId(app.getId())
                .channelId(order.getChannelId()).channelCode(order.getChannelCode())
                .orderId(order.getId()).orderNo(order.getNo())
                .merchantOrderId(order.getMerchantOrderId())
                .merchantRefundId(reqDTO.getMerchantRefundId())
                .notifyUrl(notifyUrl)
                .status(PayRefundStatusEnum.WAITING)
                .payPrice(order.getPrice()).refundPrice(reqDTO.getPrice())
                .reason(reqDTO.getReason()).userIp(reqDTO.getUserIp())
                .channelOrderNo(order.getChannelOrderNo())
                .build();
        refundRepository.save(refund);

        // 3. Call the channel. Only LOG on error — a timeout may still have succeeded; the sync job
        //    (getRefund) reconciles either way, so we must not roll back the refund row here.
        try {
            PayClient<?> client = TenantUtils.executeIgnore(
                    () -> channelService.getPayClient(order.getChannelId()));
            PayRefundChannelRespDTO resp = client.unifiedRefund(PayRefundUnifiedReqDTO.builder()
                    .outTradeNo(order.getNo())
                    .outRefundNo(refund.getNo())
                    .reason(reqDTO.getReason())
                    .payPrice(order.getPrice())
                    .refundPrice(reqDTO.getPrice())
                    .notifyUrl(genChannelRefundNotifyUrl(order.getChannelId()))
                    .orderCreateTime(orderService.getOrderExtensionByNo(order.getNo()).getCreateTime()) // A-derive: same value used as vnp_CreateDate at pay time
                    .channelOrderNo(order.getChannelOrderNo())
                    .createBy(reqDTO.getCreateBy())
                    .build());
            getSelf().notifyRefund(order.getChannelId(), resp);
        } catch (Throwable e) {
            log.error("[createRefund][refund({}) channel call failed; sync will reconcile]",
                    refund.getId(), e);
        }
        return refund.getId();
    }

    /**
     * Validate that {@code order} can be refunded by {@code reqDTO}: it exists, is paid or already
     * partially refunded, the running total would not exceed the paid amount, and no WAITING refund
     * is already in flight for it.
     */
    private PayOrderPO validateOrderCanRefund(Long appId, PayRefundCreateReqDTO reqDTO) {
        PayOrderPO order = orderService.getOrder(appId, reqDTO.getMerchantOrderId());
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (!com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum
                .isSuccessOrRefund(order.getStatus().getStatus())) {
            throw exception(REFUND_ORDER_STATUS_INVALID);
        }
        BigDecimal already = order.getRefundPrice() == null ? BigDecimal.ZERO : order.getRefundPrice();
        if (reqDTO.getPrice().add(already).compareTo(order.getPrice()) > 0) {
            throw exception(REFUND_PRICE_EXCEED);
        }
        if (refundRepository.countByAppIdAndOrderIdAndStatus(
                appId, order.getId(), PayRefundStatusEnum.WAITING) > 0) {
            throw exception(REFUND_HAS_REFUNDING);
        }
        return order;
    }

    /** Refund callback base URL + channel id; null when no refund notify base is configured. */
    private String genChannelRefundNotifyUrl(Long channelId) {
        String base = payProperties.getRefundNotifyUrl();
        return base == null ? null : base + "/" + channelId;
    }

    @Override
    public void notifyRefund(Long channelId, PayRefundChannelRespDTO notify) {
        PayChannelPO channel = TenantUtils.executeIgnore(() -> channelService.validChannel(channelId));
        TenantUtils.execute(channel.getTenantId(),
                () -> getSelf().notifyRefundInTransaction(channel, notify));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyRefundInTransaction(PayChannelPO channel, PayRefundChannelRespDTO notify) {
        if (PayRefundStatusEnum.isSuccess(notify.getStatus())) {
            notifyRefundSuccess(channel, notify);
            return;
        }
        if (PayRefundStatusEnum.isFailure(notify.getStatus())) {
            notifyRefundFailure(channel, notify);
        }
        // WAITING: nothing to do; the sync job will re-query.
    }

    private void notifyRefundSuccess(PayChannelPO channel, PayRefundChannelRespDTO notify) {
        PayRefundPO refund = refundRepository
                .findByAppIdAndNo(channel.getAppId(), notify.getOutRefundNo())
                .orElseThrow(() -> exception(REFUND_NOT_FOUND));
        if (PayRefundStatusEnum.isSuccess(refund.getStatus().getStatus())) {
            return; // already applied (duplicate callback/sync) — idempotent no-op
        }
        int updated = refundRepository.updateStatusToSuccess(
                refund.getId(), PayRefundStatusEnum.WAITING, PayRefundStatusEnum.SUCCESS,
                notify.getChannelRefundNo(), notify.getSuccessTime(), JsonUtils.toJsonString(notify));
        if (updated == 0) {
            return; // another path won the CAS — treat as duplicate, not an error
        }
        // Bump the order's running refund total and flip it to REFUND (atomic, SUCCESS|REFUND -> REFUND).
        orderService.updateOrderRefundPrice(refund.getOrderId(), refund.getRefundPrice());
        // Outbox: ALWAYS enqueue (like the order path / ). If the merchant configured no refund
        // callback, the relay ends the task as a SUCCESS no-op — see PayNotifyServiceImpl.executeNotify0.
        notifyService.createPayNotifyTask(refund);
    }

    private void notifyRefundFailure(PayChannelPO channel, PayRefundChannelRespDTO notify) {
        PayRefundPO refund = refundRepository
                .findByAppIdAndNo(channel.getAppId(), notify.getOutRefundNo())
                .orElseThrow(() -> exception(REFUND_NOT_FOUND));
        if (PayRefundStatusEnum.isFailure(refund.getStatus().getStatus())) {
            return; // idempotent no-op
        }
        int updated = refundRepository.updateStatusToFailure(
                refund.getId(), PayRefundStatusEnum.WAITING, PayRefundStatusEnum.FAILURE,
                notify.getChannelRefundNo(), JsonUtils.toJsonString(notify),
                notify.getChannelErrorCode(), notify.getChannelErrorMsg());
        if (updated == 0) {
            return;
        }
        notifyService.createPayNotifyTask(refund); // always enqueue; relay no-ops if url is blank
    }

    @Override
    public int syncRefund() {
        Instant after = Instant.now().minus(payProperties.getRefundSyncCreateTimeWithin());
        List<PayRefundPO> refunds = refundRepository
                .findTop200ByStatusAndCreateTimeGreaterThanEqualOrderByIdAsc(
                        PayRefundStatusEnum.WAITING, after);
        int resolved = 0;
        for (PayRefundPO refund : refunds) {
            resolved += syncRefund(refund) ? 1 : 0;
        }
        return resolved;
    }

    private boolean syncRefund(PayRefundPO refund) {
        try {
            PayClient<?> client = TenantUtils.executeIgnore(
                    () -> channelService.getPayClient(refund.getChannelId()));
            PayRefundChannelRespDTO resp = client.getRefund(PayRefundGetReqDTO.builder()
                    .outTradeNo(refund.getOrderNo())
                    .outRefundNo(refund.getNo())
                    .createTime(refund.getCreateTime())
                    .channelOrderNo(refund.getChannelOrderNo())
                    .build());
            notifyRefund(refund.getChannelId(), resp);
            return PayRefundStatusEnum.isSuccess(resp.getStatus())
                    || PayRefundStatusEnum.isFailure(resp.getStatus());
        } catch (Throwable e) {
            log.error("[syncRefund][refund({}) sync failed]", refund.getId(), e);
            return false;
        }
    }

    @Override
    public PayRefundPO getRefund(Long id) {
        return refundRepository.findById(id).orElse(null);
    }

    @Override
    public PageResult<PayRefundPO> getRefundPage(PayRefundPageReqDTO pageReqDTO) {
        Specification<PayRefundPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, PayRefundPO_.appId, pageReqDTO.getAppId());
            eqIfPresent(predicates, cb, root, PayRefundPO_.channelCode, pageReqDTO.getChannelCode());
            likeIfPresent(predicates, cb, root, PayRefundPO_.merchantOrderId, pageReqDTO.getMerchantOrderId());
            likeIfPresent(predicates, cb, root, PayRefundPO_.merchantRefundId, pageReqDTO.getMerchantRefundId());
            eqIfPresent(predicates, cb, root, PayRefundPO_.status, PayRefundStatusEnum.of(pageReqDTO.getStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageUtils.toPageable(pageReqDTO);
        Page<PayRefundPO> page = refundRepository.findAll(spec, pageable);
        return PageUtils.toPageResult(page);
    }


    private PayRefundServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
