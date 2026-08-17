package com.hdl.soar.module.pay.dal.entity.refund;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import com.hdl.soar.module.pay.enums.PayRefundStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A refund against a paid order. One paid {@code pay_order} may have many refunds (partial refunds
 * and repeated refunds), so {@code pay_order : pay_refund = 1 : n}.
 * <p>
 * Global (extends {@link BasePO}): same scoping as {@code pay_order}. The tenant-scoped notify task
 * created on success is placed under the channel's tenant, not the refund's (the refund has none).
 */
@Entity
@Table(name = "pay_refund")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class PayRefundPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** External refund number, generated; used as the refund id sent to the channel. */
    @Column(name = "no", nullable = false)
    private String no;

    /** Owning app id. */
    @Column(name = "app_id", nullable = false)
    private Long appId;

    /** Channel used to refund (copied from the paid order). */
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    /** Channel code (redundant copy of the order's channel code). */
    @Column(name = "channel_code")
    private String channelCode;

    /** Refunded order id. References {@code PayOrderPO#getId()}. */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** Refunded order no (redundant copy of the paid order's winning extension no). */
    @Column(name = "order_no")
    private String orderNo;

    /** Merchant's own order id, echoed back to the merchant. */
    @Column(name = "merchant_order_id", nullable = false)
    private String merchantOrderId;

    /** Merchant's own refund id — unique per app; the idempotency key for refund creation. */
    @Column(name = "merchant_refund_id", nullable = false)
    private String merchantRefundId;

    /** Where to POST the refund result to the merchant; copied from {@code app.refundNotifyUrl}. */
    @Column(name = "notify_url")
    private String notifyUrl;

    /**
     * Status.
     * <p>
     * Enum {@link PayRefundStatusEnum}
     */
    @Column(name = "status", nullable = false)
    private PayRefundStatusEnum status;

    /** Amount originally paid (redundant copy of the order's price at refund time). */
    @Column(name = "pay_price", nullable = false)
    private BigDecimal payPrice;

    /** Amount to refund (this refund only, not the running total). */
    @Column(name = "refund_price", nullable = false)
    private BigDecimal refundPrice;

    /** Human reason for the refund. */
    @Column(name = "reason")
    private String reason;

    /** Client IP that requested the refund. */
    @Column(name = "user_ip")
    private String userIp;

    /** Channel-side order number (redundant copy of the order's channel order no). */
    @Column(name = "channel_order_no")
    private String channelOrderNo;

    /** Channel-side refund number, set on success. */
    @Column(name = "channel_refund_no")
    private String channelRefundNo;

    /** Time the channel confirmed the refund. */
    @Column(name = "success_time")
    private Instant successTime;

    /** Business error code returned by the channel on a failed refund. */
    @Column(name = "channel_error_code")
    private String channelErrorCode;

    /** Business error message returned by the channel on a failed refund. */
    @Column(name = "channel_error_msg")
    private String channelErrorMsg;

    /** Raw channel sync/callback payload, kept for audit. */
    @Column(name = "channel_notify_data")
    private String channelNotifyData;

}
