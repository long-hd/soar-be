package com.hdl.soar.framework.quartz.core.service;

import java.time.Instant;

/**
 * SPI for recording job execution logs.
 *
 * <p>Deliberately an interface: this framework module knows nothing about where logs are
 * stored. A business module supplies the implementation (persisting to a table), keeping
 * the engine decoupled from persistence.
 */
public interface JobLogFrameworkService {

    /**
     * Creates a job log entry when execution begins.
     *
     * @param jobId           the job id
     * @param beginTime       execution start time
     * @param jobHandlerName  the handler's name
     * @param jobHandlerParam the handler's param
     * @param executeIndex    which attempt this is (1-based)
     * @return the created log id
     */
    Long createJobLog(Long jobId, Instant beginTime, String jobHandlerName,
                      String jobHandlerParam, Integer executeIndex);


    /**
     * Updates a job log with its result, asynchronously.
     *
     * @param logId    the log id
     * @param endTime  execution end time (passed in, since async would skew it)
     * @param duration execution duration in milliseconds
     * @param success  whether the job succeeded
     * @param result   the result data, or the root cause message on failure
     */
    void updateJobLogResultAsync(Long logId, Instant endTime, Integer duration,
                                 boolean success, String result);

}
