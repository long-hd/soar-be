package com.hdl.soar.module.pay.dal.entity.notify;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import com.hdl.soar.module.pay.enums.notify.PayNotifyStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

/**
 * Notify log — one row per delivery attempt. This is the audit trail behind the admin
 * "why did this fail" view; it is what the outbox table gives you that a broker's queue alone does not.
 * <p>
 * Global (extends {@link BasePO}).
 */
@Entity
@Table(name = "pay_notify_log")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class PayNotifyLogPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning task id. */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /** Which attempt this row records (1-based). */
    @Column(name = "notify_times", nullable = false)
    private Integer notifyTimes;

    /** Attempt outcome: {@link PayNotifyStatusEnum#SUCCESS} or {@link PayNotifyStatusEnum#FAILURE}. */
    @Column(name = "status", nullable = false)
    private PayNotifyStatusEnum status;

    /** Merchant response summary, or the error message on failure. Truncated to fit the column. */
    @Column(name = "response")
    private String response;

}
