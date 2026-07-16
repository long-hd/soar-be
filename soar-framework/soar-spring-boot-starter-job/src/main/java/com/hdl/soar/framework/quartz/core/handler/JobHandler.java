package com.hdl.soar.framework.quartz.core.handler;

/**
 * Contract for a scheduled task.
 *
 * <p>Each job is a Spring bean implementing this interface; the bean name is the job's
 * identifier, stored as {@code jobHandlerName} and used as the Quartz JobKey/TriggerKey.
 *
 * <p>The scheduling engine sits underneath this contract, so swapping engines never
 * requires rewriting jobs.
 */
public interface JobHandler {

    /**
     * Executes the task.
     *
     * @param param the task parameter
     * @return the execution result, recorded in the job log
     * @throws Exception on failure — triggers the retry logic
     */
    String execute(String param) throws Exception;

}
