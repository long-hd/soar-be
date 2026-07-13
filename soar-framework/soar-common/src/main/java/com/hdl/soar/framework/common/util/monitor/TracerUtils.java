package com.hdl.soar.framework.common.util.monitor;

import org.slf4j.MDC;

/**
 * Distributed tracing utility class.
 * <p>
 * Reads the current trace ID from SLF4J {@link MDC}, which Micrometer Tracing
 * populates per request under the {@code "traceId"} key (Spring Boot 3 default).
 * Returns an empty string when no span is in scope (e.g. outside an HTTP request,
 * or on a pooled thread that did not receive the correlation context).
 */
public class TracerUtils {

    /** MDC key Micrometer Tracing uses for the trace ID. */
    private static final String MDC_KEY_TRACE_ID = "traceId";

    /**
     * Private constructor to prevent instantiation.
     */
    private TracerUtils() {
    }

    /**
     * Gets the current distributed trace ID.
     *
     * @return the trace ID, or empty string if none is active
     */
    public static String getTraceId() {
        String traceId = MDC.get(MDC_KEY_TRACE_ID);
        return traceId != null ? traceId : "";
    }

}

