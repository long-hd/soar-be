package com.hdl.soar.module.pay.dal.entity.app;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

/**
 * Payment application.
 * <p>
 * An app is a money-collecting identity registered in the gateway. A business system authenticates
 * with its {@link #appKey} and calls the gateway to collect money; the app also defines the
 * merchant-order-id uniqueness space and where results are reported back ({@link #orderNotifyUrl}).
 * <p>
 * Extends {@link BasePO} (not tenant-scoped): an app is a global gateway identity and carries no
 * per-tenant secret. Per-tenant secrets live on {@code PayChannelPO}.
 * <p>
 * {@code PayAppPO : PayChannelPO = 1 : n}.
 */
@Entity
@Table(name = "pay_app")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class PayAppPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * App key: the identifier a business system presents to authenticate with the gateway. Unique.
     */
    @Column(name = "app_key", nullable = false)
    private String appKey;

    /**
     * Display name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Status.
     * <p>
     * Enum {@link CommonStatusEnum}
     */
    @Column(name = "status", nullable = false)
    private CommonStatusEnum status;

    /**
     * Remark.
     */
    @Column(name = "remark")
    private String remark;

    /**
     * Callback URL invoked when a payment order succeeds.
     */
    @Column(name = "order_notify_url", nullable = false)
    private String orderNotifyUrl;

    /**
     * Callback URL invoked when a refund completes. Optional until refunds are implemented.
     */
    @Column(name = "refund_notify_url")
    private String refundNotifyUrl;

    /**
     * Callback URL invoked when a transfer completes. Optional until transfers are implemented.
     */
    @Column(name = "transfer_notify_url")
    private String transferNotifyUrl;

}
