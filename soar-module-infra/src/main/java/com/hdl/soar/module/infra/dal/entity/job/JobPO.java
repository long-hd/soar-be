package com.hdl.soar.module.infra.dal.entity.job;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import com.hdl.soar.module.infra.enums.job.JobStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

/**
 * Scheduled job configuration.
 * <p>
 * Extends {@link BasePO} (not TenantBasePO) — a job's config is global. Running a job
 * once per tenant is a runtime concern, handled by the per-tenant job aspect.
 */
@Entity
@Table(name = "infra_job")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class JobPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Display name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * See {@link JobStatusEnum}.
     */
    @Column(name = "status", nullable = false)
    private JobStatusEnum status;

    /**
     * Name of the JobHandler Spring bean. Also used as the Quartz JobKey/TriggerKey.
     */
    @Column(name = "handler_name", nullable = false)
    private String handlerName;

    /**
     * Parameter passed to the handler.
     */
    @Column(name = "handler_param")
    private String handlerParam;

    /**
     * CRON expression driving the schedule.
     */
    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    /**
     * Number of retries on failure; 0 means no retry.
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    /**
     * Interval between retries in milliseconds; 0 means no wait.
     */
    @Column(name = "retry_interval", nullable = false)
    private Integer retryInterval;

    /**
     * Alerting threshold in milliseconds; null means no monitoring.
     * <p>
     * Note: this is a threshold for warning that a run took too long — it does NOT cancel
     * the job. Currently stored but not yet acted upon.
     */
    @Column(name = "monitor_timeout")
    private Integer monitorTimeout;

}
