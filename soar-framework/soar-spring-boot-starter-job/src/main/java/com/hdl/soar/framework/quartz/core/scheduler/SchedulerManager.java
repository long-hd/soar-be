package com.hdl.soar.framework.quartz.core.scheduler;

import com.hdl.soar.framework.quartz.core.enums.JobDataKeyEnum;
import com.hdl.soar.framework.quartz.core.handler.JobHandlerInvoker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.quartz.*;

import static com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;
import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * Manager over Quartz's {@link Scheduler}, responsible for scheduling jobs.
 *
 * <p>For simplicity, jobHandlerName is the single identifier: it is both the
 * {@link JobDetail} key and the {@link Trigger} key, and it is also the Spring bean name
 * that {@link JobHandlerInvoker} looks up.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SchedulerManager {

    Scheduler scheduler;

    /**
     * Adds a job to Quartz.
     *
     * @param jobId the job ID
     * @param jobHandlerName the name of the job handler
     * @param jobHandlerParam the job handler parameter
     * @param cronExpression the CRON expression
     * @param retryCount the number of retry attempts
     * @param retryInterval the retry interval
     * @throws SchedulerException if an error occurs while adding the job
     */
    public void addJob(Long jobId, String jobHandlerName, String jobHandlerParam, String cronExpression,
                       Integer retryCount, Integer retryInterval) throws SchedulerException {
        validateScheduler();

        // Create the JobDetail object
        JobDetail jobDetail = JobBuilder.newJob(JobHandlerInvoker.class)
                .usingJobData(JobDataKeyEnum.JOB_ID.name(), jobId)
                .usingJobData(JobDataKeyEnum.JOB_HANDLER_NAME.name(), jobHandlerName)
                .withIdentity(jobHandlerName)
                .build();

        // Create the Trigger object
        Trigger trigger = buildTrigger(jobHandlerName, jobHandlerParam, cronExpression, retryCount, retryInterval);

        // Schedule the job
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Updates an existing job in Quartz.
     *
     * @param jobHandlerName the name of the job handler
     * @param jobHandlerParam the job handler parameter
     * @param cronExpression the CRON expression
     * @param retryCount the number of retry attempts
     * @param retryInterval the retry interval
     * @throws SchedulerException if an error occurs while updating the job
     */
    public void updateJob(String jobHandlerName, String jobHandlerParam, String cronExpression,
                          Integer retryCount, Integer retryInterval) throws SchedulerException {
        validateScheduler();

        // Create a new Trigger object
        Trigger newTrigger = buildTrigger(jobHandlerName, jobHandlerParam, cronExpression, retryCount, retryInterval);

        // Reschedule the job
        scheduler.rescheduleJob(new TriggerKey(jobHandlerName), newTrigger);
    }

    /**
     * Deletes a job from Quartz.
     *
     * @param jobHandlerName the name of the job handler
     * @throws SchedulerException if an error occurs while deleting the job
     */
    public void deleteJob(String jobHandlerName) throws SchedulerException {
        validateScheduler();

        // Pause the trigger
        scheduler.pauseTrigger(new TriggerKey(jobHandlerName));

        // Unschedule and delete the job
        scheduler.unscheduleJob(new TriggerKey(jobHandlerName));
        scheduler.deleteJob(new JobKey(jobHandlerName));
    }

    /**
     * Pauses a job in Quartz.
     *
     * @param jobHandlerName the name of the job handler
     * @throws SchedulerException if an error occurs while pausing the job
     */
    public void pauseJob(String jobHandlerName) throws SchedulerException {
        validateScheduler();
        scheduler.pauseJob(new JobKey(jobHandlerName));
    }

    /**
     * Resumes a job in Quartz.
     *
     * @param jobHandlerName the name of the job handler
     * @throws SchedulerException if an error occurs while resuming the job
     */
    public void resumeJob(String jobHandlerName) throws SchedulerException {
        validateScheduler();
        scheduler.resumeJob(new JobKey(jobHandlerName));
        scheduler.resumeTrigger(new TriggerKey(jobHandlerName));
    }

    /**
     * Triggers a job once, immediately. No retry data is attached: a manual run should not retry.
     *
     * @param jobId the job ID
     * @param jobHandlerName the name of the job handler
     * @param jobHandlerParam the job handler parameter
     * @throws SchedulerException if an error occurs while triggering the job
     */
    public void triggerJob(Long jobId, String jobHandlerName, String jobHandlerParam) throws SchedulerException {
        validateScheduler();

        // Trigger the job immediately
        JobDataMap data = new JobDataMap();
        data.put(JobDataKeyEnum.JOB_ID.name(), jobId);
        data.put(JobDataKeyEnum.JOB_HANDLER_NAME.name(), jobHandlerName);
        data.put(JobDataKeyEnum.JOB_HANDLER_PARAM.name(), jobHandlerParam);
        scheduler.triggerJob(new JobKey(jobHandlerName), data);
    }

    private Trigger buildTrigger(String jobHandlerName, String jobHandlerParam, String cronExpression,
                                 Integer retryCount, Integer retryInterval) {
        return TriggerBuilder.newTrigger()
                .withIdentity(jobHandlerName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .usingJobData(JobDataKeyEnum.JOB_HANDLER_PARAM.name(), jobHandlerParam)
                .usingJobData(JobDataKeyEnum.JOB_RETRY_COUNT.name(), retryCount)
                .usingJobData(JobDataKeyEnum.JOB_RETRY_INTERVAL.name(), retryInterval)
                .build();
    }

    private void validateScheduler() {
        if (scheduler == null) {
            throw exception0(NOT_IMPLEMENTED.getCode(), "Scheduled jobs are disabled");
        }
    }

}
