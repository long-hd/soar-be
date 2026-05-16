package com.hdl.soar.framework.common.util.monitor;

import org.apache.skywalking.apm.toolkit.trace.TraceContext;

/**
 * Distributed tracing utility class.
 *
 * Since every starter needs to use this utility, it is placed under the util package
 * in the common module.
 *
 */
public class TracerUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private TracerUtils() {
    }

    /**
     * Gets the distributed trace ID, directly returning SkyWalking's TraceId.
     * Returns an empty string if it does not exist!!!
     *
     * @return the distributed trace ID
     */
    public static String getTraceId() {
        return TraceContext.traceId();
    }

}

