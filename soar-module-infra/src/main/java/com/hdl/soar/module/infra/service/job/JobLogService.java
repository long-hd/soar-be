package com.hdl.soar.module.infra.service.job;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.infra.controller.admin.job.dto.log.JobLogPageReqDTO;
import com.hdl.soar.module.infra.dal.entity.job.JobLogPO;

import java.time.Instant;

/**
 * Business service for job execution logs (query + retention).
 */
public interface JobLogService {

    /**
     * Records the start of an execution; returns the new log id.
     */
    Long createJobLog(Long jobId, Instant beginTime, String handlerName,
                      String handlerParam, Integer executeIndex);

    /**
     * Fills in the outcome of a previously-created log.
     */
    void updateJobLogResult(Long logId, Instant endTime, Integer duration,
                            boolean success, String result);

    /**
     * Deletes logs older than {@code exceedDay} days, in batches of {@code deleteLimit}.
     *
     * @return total number of rows deleted
     */
    Integer cleanJobLog(Integer exceedDay, Integer deleteLimit);

    JobLogPO getJobLog(Long id);

    PageResult<JobLogPO> getJobLogPage(JobLogPageReqDTO pageReqDTO);

}
