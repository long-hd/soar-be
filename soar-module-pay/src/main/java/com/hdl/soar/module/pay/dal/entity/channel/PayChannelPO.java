package com.hdl.soar.module.pay.dal.entity.channel;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

/**
 * Payment channel: one real payment rail (e.g. VNPay, MoMo) configured under an app.
 * <p>
 * Extends {@link TenantBasePO} because a channel holds per-tenant secret credentials in
 * {@link #config}: one tenant's rail keys must never be visible to another. This is the reason the
 * channel is tenant-scoped while the app and orders are global.
 * <p>
 * {@code PayAppPO : PayChannelPO = 1 : n}.
 */
@Entity
@Table(name = "pay_channel")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class PayChannelPO extends TenantBasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owning app id.
     * <p>
     * References {@link com.hdl.soar.module.pay.dal.entity.app.PayAppPO#getId()}.
     */
    @Column(name = "app_id", nullable = false)
    private Long appId;

    /**
     * Channel code.
     * <p>
     * Enum {@link com.hdl.soar.module.pay.enums.PayChannelEnum}
     */
    @Column(name = "code", nullable = false)
    private String code;

    /**
     * Status.
     * <p>
     * Enum {@link CommonStatusEnum}
     */
    @Column(name = "status", nullable = false)
    private CommonStatusEnum status;

    /**
     * Channel fee rate, as a percentage (e.g. 0.5 means 0.5%).
     */
    @Column(name = "fee_rate", nullable = false)
    private Double feeRate;

    /**
     * Remark.
     */
    @Column(name = "remark")
    private String remark;

    /**
     * Channel configuration as raw JSON (rail credentials, merchant id, ...).
     * <p>
     * Stored as a JSON string in this slice. A typed, polymorphic client-config mapping is
     * introduced when the {@code PayClient} abstraction lands in a later slice.
     */
    @Column(name = "config", nullable = false)
    private String config;

}
