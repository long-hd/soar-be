package com.hdl.soar.framework.apilog.core.annotation;

import com.hdl.soar.framework.common.enums.OperateTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API access log annotation.
 * <p>
 * By default, the filter logs ALL API requests automatically.
 * Use this annotation to customize or disable logging for specific endpoints.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiAccessLog {

    // ========== Switches ==========

    /**
     * Whether to enable logging for this endpoint.
     * Set to false to skip logging entirely.
     */
    boolean enable() default true;

    /**
     * Whether to log request parameters (query string + body).
     * Default true — request data is usually small.
     */
    boolean requestEnable() default true;

    /**
     * Whether to log response body.
     * Default false — response data can be large.
     */
    boolean responseEnable() default false;

    /**
     * Sensitive parameter keys to sanitize from request/response.
     * These keys (and their values) are removed before logging.
     * Built-in keys (password, token, etc.) are always sanitized.
     */
    String[] sanitizeKeys() default {};

    // ========== Module metadata ==========

    /**
     * Operate module name.
     * When empty, falls back to {@link io.swagger.v3.oas.annotations.tags.Tag#name()}.
     */
    String operateModule() default "";

    /**
     * Operate name (action description).
     * When empty, falls back to {@link io.swagger.v3.oas.annotations.Operation#summary()}.
     */
    String operateName() default "";

    /**
     * Operate type.
     * When empty, auto-inferred from HTTP method (GET->GET, POST->CREATE, etc.).
     * <p>
     * Array type because annotation defaults cannot be null.
     */
    OperateTypeEnum[] operateType() default {};

}