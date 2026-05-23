package com.hdl.soar.framework.common.enums;

/**
 * Enum for Web Filter order, ensuring filters execute in the expected sequence.
 * <p>
 * Since every starter module may use this utility class,
 * it is placed under the common module's enums package.
 */
public interface WebFilterOrderEnum {

    int CORS_FILTER = Integer.MIN_VALUE;

    int TRACE_FILTER = CORS_FILTER + 1;

    int REQUEST_BODY_CACHE_FILTER = Integer.MIN_VALUE + 500;

    int API_ENCRYPT_FILTER = REQUEST_BODY_CACHE_FILTER + 1;

    // OrderedRequestContextFilter defaults to -105, used for i18n context, etc.

    int TENANT_CONTEXT_FILTER = -104; // Must be placed before ApiAccessLogFilter

    int API_ACCESS_LOG_FILTER = -103; // Must be placed after RequestBodyCacheFilter

    int XSS_FILTER = -102; // Must be placed after RequestBodyCacheFilter

    // Spring Security filter defaults to -100, see SecurityProperties in Spring Boot

    int TENANT_SECURITY_FILTER = -99; // Must be placed after Spring Security filters

    int FLOWABLE_FILTER = -98; // Must be placed after Spring Security filters

    int DEMO_FILTER = Integer.MAX_VALUE;

}

