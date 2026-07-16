package com.hdl.soar.framework.quartz.core.enums;

/**
 * Keys used in Quartz's JobDataMap to carry a job's metadata to the invoker.
 */
public enum JobDataKeyEnum {

    JOB_ID,
    JOB_HANDLER_NAME,
    JOB_HANDLER_PARAM,
    JOB_RETRY_COUNT,      // max retry attempts
    JOB_RETRY_INTERVAL,   // interval between retries, in milliseconds

}
