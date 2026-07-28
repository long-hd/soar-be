package com.hdl.soar.module.infra.dal.postgres.job;

import com.hdl.soar.module.infra.dal.entity.job.JobLogPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface JobLogRepository extends JpaRepository<JobLogPO, Long>, JpaSpecificationExecutor<JobLogPO> {

    /**
     * Physically deletes logs older than {@code expireTime}, at most {@code limit} rows per call.
     * <p>
     * Retention clean-up: these rows are operational, dump-after-use data, so they are removed
     * for real (not soft-deleted) — soft-deleting would defeat the point, as the table would
     * never actually shrink.
     * <p>
     * Postgres has no {@code LIMIT} on {@code DELETE}, so the batch is picked by a subquery on
     * the primary key. Native query because of that DELETE-with-subquery form.
     */
    @Modifying
    @Query(value = "DELETE FROM infra_job_log WHERE id IN " +
            "(SELECT id FROM infra_job_log WHERE create_time < :expireTime ORDER BY create_time LIMIT :limit)",
            nativeQuery = true)
    int deleteByCreateTimeLtWithLimit(@Param("expireTime") Instant expireTime,
                                      @Param("limit") int limit);

}
