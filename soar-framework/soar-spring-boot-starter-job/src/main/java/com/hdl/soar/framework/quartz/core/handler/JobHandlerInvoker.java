package com.hdl.soar.framework.quartz.core.handler;

import com.hdl.soar.framework.quartz.core.enums.JobDataKeyEnum;
import com.hdl.soar.framework.quartz.core.service.JobLogFrameworkService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.Duration;
import java.time.Instant;

import static cn.hutool.core.exceptions.ExceptionUtil.getRootCauseMessage;

/**
 * The single Quartz job class: every scheduled job routes through here, which then
 * dispatches to the right {@link JobHandler} bean.
 *
 * <p>{@link DisallowConcurrentExecution} stops one job from overlapping itself.
 * {@link PersistJobDataAfterExecution} persists JobDataMap changes after each run.
 *
 * <p>Note the handler is looked up from the ApplicationContext, so what we invoke is the
 * Spring PROXY — this is what lets method annotations on the handler (such as the
 * per-tenant job annotation) take effect. Calling the handler directly would bypass the
 * proxy and silently skip those aspects.
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobHandlerInvoker extends QuartzJobBean {

    ApplicationContext applicationContext;

    JobLogFrameworkService jobLogFrameworkService;

    @Override
    protected void executeInternal(JobExecutionContext executionContext) throws JobExecutionException {
        // 1. Read the job's metadata out of the JobDataMap
        Long jobId = executionContext.getMergedJobDataMap().getLong(JobDataKeyEnum.JOB_ID.name());
        String jobHandlerName = executionContext.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_NAME.name());
        String jobHandlerParam = executionContext.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_PARAM.name());
        int refireCount = executionContext.getRefireCount();
        int retryCount = (Integer) executionContext.getMergedJobDataMap()
                .getOrDefault(JobDataKeyEnum.JOB_RETRY_COUNT.name(), 0);
        int retryInterval = (Integer) executionContext.getMergedJobDataMap()
                .getOrDefault(JobDataKeyEnum.JOB_RETRY_INTERVAL.name(), 0);

        // 2. Run the task, capturing either its result or its failure
        Long jobLogId = null;
        Instant startTime = Instant.now();
        String data = null;
        Throwable exception = null;
        try {
            jobLogId = jobLogFrameworkService.createJobLog(jobId, startTime, jobHandlerName,
                    jobHandlerParam, refireCount + 1);
            data = executeHandler(jobHandlerName, jobHandlerParam);
        } catch (Throwable ex) {
            exception = ex;
        }

        // 3. Record the outcome (never let logging failures mask the job's own result)
        updateJobLogResultAsync(jobLogId, startTime, data, exception, executionContext);

        // 4. Retry or give up
        handleException(exception, refireCount, retryCount, retryInterval);
    }

    private String executeHandler(String jobHandlerName, String jobHandlerParam) throws Exception {
        // getBean returns the proxy — required for the handler's own annotations to apply
        JobHandler jobHandler = applicationContext.getBean(jobHandlerName, JobHandler.class);
        return jobHandler.execute(jobHandlerParam);
    }

    private void updateJobLogResultAsync(Long jobLogId, Instant startTime, String data,
                                         Throwable exception, JobExecutionContext executionContext) {
        Instant endTime = Instant.now();
        boolean success = exception == null;
        if (!success) {
            data = getRootCauseMessage(exception);
        }
        try {
            jobLogFrameworkService.updateJobLogResultAsync(jobLogId, endTime,
                    (int) Duration.between(startTime, endTime).toMillis(), success, data);
        } catch (Exception ex) {
            log.error("[updateJobLogResultAsync][job({}) logId({}) failed to record result({}/{})]",
                    executionContext.getJobDetail().getKey(), jobLogId, success, data, ex);
        }
    }

    private void handleException(Throwable exception, int refireCount, int retryCount,
                                 int retryInterval) throws JobExecutionException {
        if (exception == null) {
            return;
        }
        // Retry budget exhausted -> propagate, Quartz marks the run as failed
        if (refireCount >= retryCount) {
            throw new JobExecutionException(exception);
        }
        // Otherwise wait, then ask Quartz to refire immediately.
        // Sleeping keeps this simple: failing jobs are rare and few at any one time.
        if (retryInterval > 0) {
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // restore the flag, then stop retrying
                throw new JobExecutionException(exception);
            }
        }
        throw new JobExecutionException(exception, true); // refireImmediately = true
    }

}
