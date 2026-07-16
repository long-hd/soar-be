package com.hdl.soar.framework.quartz.core.service;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

/**
 * Fallback {@link JobLogFrameworkService} that only logs via slf4j.
 *
 * <p>Registered only when no other implementation exists, so a business module can supply a
 * persisting implementation later without touching this module.
 */
@Slf4j
public class NoOpJobLogFrameworkService implements JobLogFrameworkService {

    @Override
    public Long createJobLog(Long jobId, Instant beginTime, String jobHandlerName,
                             String jobHandlerParam, Integer executeIndex) {
        log.info("[createJobLog][job({}) handler({}) param({}) attempt({}) started]",
                jobId, jobHandlerName, jobHandlerParam, executeIndex);
        return null; // no persistence, so no log id
    }

    @Override
    public void updateJobLogResultAsync(Long logId, Instant endTime, Integer duration,
                                        boolean success, String result) {
        log.info("[updateJobLogResultAsync][finished in {}ms, success({}), result({})]",
                duration, success, result);
    }

}
