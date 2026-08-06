package com.hdl.soar.module.pay.dal.entity.notify;

import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import com.hdl.soar.module.pay.enums.notify.PayNotifyStatusEnum;
import com.hdl.soar.module.pay.enums.notify.PayNotifyTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * Notify task — the outbox row.
 * <p>
 * Inserted in the SAME transaction that transitions an order to SUCCESS, so a task exists if and only
 * if the order was really paid (no dual write). A relay (afterCommit fast-path + poll job) then keeps
 * calling the merchant's {@link #notifyUrl} until it acknowledges or {@link #maxNotifyTimes} is hit.
 * <p>
 * Tenant-scoped (extends {@link TenantBasePO}): a task is created under the tenant of the channel that
 * drove the success, and the poll job iterates per tenant.
 */
@Entity
@Table(name = "pay_notify_task")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class PayNotifyTaskPO extends TenantBasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning app id. */
    @Column(name = "app_id", nullable = false)
    private Long appId;

    /** Kind of business object; only {@link PayNotifyTypeEnum#ORDER} today. */
    @Column(name = "type", nullable = false)
    private PayNotifyTypeEnum type;

    /** Id of the business object (the order id). */
    @Column(name = "data_id", nullable = false)
    private Long dataId;

    /** Merchant's own order id, echoed back so the merchant can correlate. */
    @Column(name = "merchant_order_id", nullable = false)
    private String merchantOrderId;

    /** Where to POST the notify. Copied from the order at enqueue time. */
    @Column(name = "notify_url", nullable = false)
    private String notifyUrl;

    /** Task status. {@link PayNotifyStatusEnum#WAITING} while retrying; SUCCESS/FAILURE are terminal. */
    @Column(name = "status", nullable = false)
    private PayNotifyStatusEnum status;

    /** When the relay should attempt (or re-attempt) delivery. Set to now on insert. */
    @Column(name = "next_notify_time", nullable = false)
    private Instant nextNotifyTime;

    /** When the last attempt ran; null before the first attempt. */
    @Column(name = "last_execute_time")
    private Instant lastExecuteTime;

    /** Attempts made so far. */
    @Column(name = "notify_times", nullable = false)
    private Integer notifyTimes;

    /** Attempt ceiling (initial attempt + retries). Task goes FAILURE once reached. */
    @Column(name = "max_notify_times", nullable = false)
    private Integer maxNotifyTimes;

}
