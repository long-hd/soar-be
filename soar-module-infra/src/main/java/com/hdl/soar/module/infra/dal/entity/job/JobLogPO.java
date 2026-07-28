package com.hdl.soar.module.infra.dal.entity.job;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import com.hdl.soar.module.infra.enums.job.JobLogStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * Execution log for a scheduled job — one row per attempt.
 * <p>
 * Global ({@link BasePO}, no tenant). {@code handlerName}/{@code handlerParam} are
 * denormalized snapshots so a log remains readable after its job changes or is removed.
 */
@Entity
@Table(name = "infra_job_log")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class JobLogPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The job this run belongs to (references {@code infra_job.id}).
     */
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    /**
     * Handler bean name (snapshot).
     */
    @Column(name = "handler_name", nullable = false)
    private String handlerName;

    /**
     * Handler parameter (snapshot).
     */
    @Column(name = "handler_param")
    private String handlerParam;

    /**
     * Which attempt this is; > 1 means a retry.
     */
    @Column(name = "execute_index", nullable = false)
    private Integer executeIndex;

    /**
     * Execution start time.
     */
    @Column(name = "begin_time", nullable = false)
    private Instant beginTime;

    /**
     * Execution end time; null while still running.
     */
    @Column(name = "end_time")
    private Instant endTime;

    /**
     * Duration in milliseconds; null while still running.
     */
    @Column(name = "duration")
    private Integer duration;

    /**
     * See {@link JobLogStatusEnum}.
     */
    @Column(name = "status", nullable = false)
    private JobLogStatusEnum status;

    /**
     * Result data on success, or the root cause message on failure; null while still running.
     */
    @Column(name = "result")
    private String result;

}
