package com.hdl.soar.module.infra.service.job;

import com.hdl.soar.framework.quartz.core.service.JobLogFrameworkService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Bridges the scheduler's log SPI to the business {@link JobLogService}.
 *
 * <p>Keeping this separate from {@link JobLogService} means the scheduler module depends
 * only on the SPI, never on the business service.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobLogFrameworkServiceImpl implements JobLogFrameworkService {

    JobLogService jobLogService;

    @Override
    public Long createJobLog(Long jobId, Instant beginTime, String jobHandlerName,
                             String jobHandlerParam, Integer executeIndex) {
        return jobLogService.createJobLog(jobId, beginTime, jobHandlerName, jobHandlerParam, executeIndex);
    }

    @Override
    public void updateJobLogResultAsync(Long logId, Instant endTime, Integer duration,
                                        boolean success, String result) {
        jobLogService.updateJobLogResult(logId, endTime, duration, success, result);
    }

}
