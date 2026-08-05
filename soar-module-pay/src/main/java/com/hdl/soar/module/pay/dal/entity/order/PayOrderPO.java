package com.hdl.soar.module.pay.dal.entity.order;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import com.hdl.soar.module.pay.enums.PayCurrencyEnum;
import com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment order: one receivable — a single request to collect money for a merchant order.
 * <p>
 * An order is created before any channel is chosen and holds the total {@link #status}. Each attempt
 * to pay it through a channel is a {@link PayOrderExtensionPO}; when one attempt succeeds, this order
 * moves to {@code SUCCESS} and {@link #extensionId}/{@link #no} point at the winning attempt.
 * <p>
 * Global (extends {@link BasePO}, not tenant-scoped): kept consistent with the reference design where
 * the gateway operates orders centrally; per-tenant secrets live only on the channel.
 */
@Entity
@Table(name = "pay_order")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class PayOrderPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning app id. */
    @Column(name = "app_id", nullable = false)
    private Long appId;

    /** Winning channel id, set when the order becomes SUCCESS. */
    @Column(name = "channel_id")
    private Long channelId;

    /** Winning channel code, set when the order becomes SUCCESS. */
    @Column(name = "channel_code")
    private String channelCode;

    /** Merchant order id — unique per app. */
    @Column(name = "merchant_order_id", nullable = false)
    private String merchantOrderId;

    /** Product title. */
    @Column(name = "subject", nullable = false)
    private String subject;

    /** Product description. */
    @Column(name = "body")
    private String body;

    /** Callback URL to notify the merchant on success, copied from the app. */
    @Column(name = "notify_url", nullable = false)
    private String notifyUrl;

    /** Amount to collect. */
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    /**
     * Settlement currency.
     * <p>
     * Enum {@link PayCurrencyEnum}
     */
    @Column(name = "currency", nullable = false)
    private PayCurrencyEnum currency;

    /** Channel fee rate as a percentage, redundant copy of the channel's rate at success time. */
    @Column(name = "channel_fee_rate")
    private Double channelFeeRate;

    /** Channel fee amount, computed at success time. */
    @Column(name = "channel_fee_price")
    private BigDecimal channelFeePrice;

    /**
     * Status.
     * <p>
     * Enum {@link PayOrderStatusEnum}
     */
    @Column(name = "status", nullable = false)
    private PayOrderStatusEnum status;

    /** Client IP that created the order. */
    @Column(name = "user_ip")
    private String userIp;

    /** Order expiry time. */
    @Column(name = "expire_time")
    private Instant expireTime;

    /** Time the order was paid. */
    @Column(name = "success_time")
    private Instant successTime;

    /** Winning extension id. References {@link PayOrderExtensionPO#getId()}. */
    @Column(name = "extension_id")
    private Long extensionId;

    /** Winning extension no. References {@link PayOrderExtensionPO#getNo()}. */
    @Column(name = "no")
    private String no;

    /** Total refunded amount. */
    @Column(name = "refund_price", nullable = false)
    private BigDecimal refundPrice;

    /** Channel-side user id (e.g. wallet/account id at the rail). */
    @Column(name = "channel_user_id")
    private String channelUserId;

    /** Channel-side order number. */
    @Column(name = "channel_order_no")
    private String channelOrderNo;

}
