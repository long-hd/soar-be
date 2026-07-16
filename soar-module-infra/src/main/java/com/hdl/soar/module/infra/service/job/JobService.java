package com.hdl.soar.module.infra.service.job;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.infra.controller.admin.job.dto.job.JobPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.job.dto.job.JobSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.job.JobPO;
import jakarta.validation.Valid;
import org.quartz.SchedulerException;

import java.util.List;

/**
 * Service for scheduled jobs. Every mutation keeps two stores in step:
 * {@code infra_job} (the config) and Quartz (the live schedule).
 */
public interface JobService {

    Long createJob(@Valid JobSaveReqDTO createReqDTO) throws SchedulerException;

    void updateJob(@Valid JobSaveReqDTO updateReqDTO) throws SchedulerException;

    /**
     * Enables or pauses a job.
     */
    void updateJobStatus(Long id, Integer status) throws SchedulerException;

    /**
     * Runs a job once, immediately, outside its schedule.
     */
    void triggerJob(Long id) throws SchedulerException;

    /**
     * Re-registers every job from the database into Quartz.
     * <p>
     * An escape hatch for when the two stores drift apart.
     */
    void syncJob() throws SchedulerException;

    void deleteJob(Long id) throws SchedulerException;

    void deleteJobList(List<Long> ids) throws SchedulerException;

    JobPO getJob(Long id);

    PageResult<JobPO> getJobPage(JobPageReqDTO pageReqDTO);

}
