package com.hdl.soar.module.infra.controller.admin.job;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.quartz.core.util.CronUtils;
import com.hdl.soar.module.infra.controller.admin.job.dto.job.JobPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.job.dto.job.JobRespDTO;
import com.hdl.soar.module.infra.controller.admin.job.dto.job.JobSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.job.JobPO;
import com.hdl.soar.module.infra.mapper.job.JobMapper;
import com.hdl.soar.module.infra.service.job.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.quartz.SchedulerException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Scheduled Job")
@Validated
@RestController
@RequestMapping("/infra/job")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobController {

    JobService jobService;

    @PostMapping("/create")
    @Operation(summary = "Create job")
    @PreAuthorize("@ss.hasPermission('infra:job:create')")
    public CommonResult<Long> createJob(@Valid @RequestBody JobSaveReqDTO createReqDTO)
            throws SchedulerException {
        return success(jobService.createJob(createReqDTO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update job")
    @PreAuthorize("@ss.hasPermission('infra:job:update')")
    public CommonResult<Boolean> updateJob(@Valid @RequestBody JobSaveReqDTO updateReqDTO)
            throws SchedulerException {
        jobService.updateJob(updateReqDTO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "Enable or pause a job")
    @Parameters({
            @Parameter(name = "id", description = "Job ID", required = true, example = "1024"),
            @Parameter(name = "status", description = "Status: 1=enable, 2=pause", required = true, example = "1")
    })
    @PreAuthorize("@ss.hasPermission('infra:job:update')")
    public CommonResult<Boolean> updateJobStatus(@RequestParam("id") Long id,
                                                 @RequestParam("status") Integer status)
            throws SchedulerException {
        jobService.updateJobStatus(id, status);
        return success(true);
    }

    @PutMapping("/trigger")
    @Operation(summary = "Trigger a job once, immediately")
    @Parameter(name = "id", description = "Job ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:job:trigger')")
    public CommonResult<Boolean> triggerJob(@RequestParam("id") Long id) throws SchedulerException {
        jobService.triggerJob(id);
        return success(true);
    }

    @PutMapping("/sync")
    @Operation(summary = "Re-register all jobs from the database into the scheduler")
    @PreAuthorize("@ss.hasPermission('infra:job:update')")
    public CommonResult<Boolean> syncJob() throws SchedulerException {
        jobService.syncJob();
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete job")
    @Parameter(name = "id", description = "Job ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:job:delete')")
    public CommonResult<Boolean> deleteJob(@RequestParam("id") Long id) throws SchedulerException {
        jobService.deleteJob(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "Bulk delete jobs")
    @Parameter(name = "ids", description = "Job IDs (comma-separated)", required = true)
    @PreAuthorize("@ss.hasPermission('infra:job:delete')")
    public CommonResult<Boolean> deleteJobList(@RequestParam("ids") List<Long> ids)
            throws SchedulerException {
        jobService.deleteJobList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get job")
    @Parameter(name = "id", description = "Job ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<JobRespDTO> getJob(@RequestParam("id") Long id) {
        JobPO job = jobService.getJob(id);
        return success(JobMapper.INSTANCE.toDTO(job));
    }

    @GetMapping("/page")
    @Operation(summary = "Get job page")
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<PageResult<JobRespDTO>> getJobPage(@Valid JobPageReqDTO pageReqDTO) {
        PageResult<JobPO> pageResult = jobService.getJobPage(pageReqDTO);
        return success(new PageResult<>(JobMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @GetMapping("/get-next-times")
    @Operation(summary = "Preview the next execution times for a CRON expression")
    @Parameters({
            @Parameter(name = "cronExpression", description = "CRON expression", required = true, example = "0 0 2 * * ?"),
            @Parameter(name = "count", description = "How many times to return", example = "5")
    })
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<List<Instant>> getJobNextTimes(
            @RequestParam("cronExpression") String cronExpression,
            @RequestParam(value = "count", required = false, defaultValue = "5") Integer count) {
        return success(CronUtils.getNextTimes(cronExpression, count));
    }

}
