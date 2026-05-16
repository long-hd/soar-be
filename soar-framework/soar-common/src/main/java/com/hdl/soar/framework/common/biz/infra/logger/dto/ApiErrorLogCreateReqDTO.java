package com.hdl.soar.framework.common.biz.infra.logger.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

/**
 * API error log
 */
@Data
public class ApiErrorLogCreateReqDTO {

    /**
     * Trace ID
     */
    private String traceId;

    /**
     * User ID
     */
    private Long userId;

    /**
     * User type
     */
    private Integer userType;

    /**
     * Application name
     */
    @NotNull(message = "Application name cannot be null")
    private String applicationName;

    /**
     * HTTP request method
     */
    @NotNull(message = "HTTP request method cannot be null")
    private String requestMethod;

    /**
     * Request URL
     */
    @NotNull(message = "Request URL cannot be null")
    private String requestUrl;

    /**
     * Request parameters
     */
    @NotNull(message = "Request parameters cannot be null")
    private String requestParams;

    /**
     * User IP address
     */
    @NotNull(message = "IP cannot be null")
    private String userIp;

    /**
     * User-Agent
     */
    @NotNull(message = "User-Agent cannot be null")
    private String userAgent;

    /**
     * Exception time
     */
    @NotNull(message = "Exception time cannot be null")
    private Instant exceptionTime;

    /**
     * Exception name
     */
    @NotNull(message = "Exception name cannot be null")
    private String exceptionName;

    /**
     * Fully qualified exception class name
     */
    @NotNull(message = "Exception class name cannot be null")
    private String exceptionClassName;

    /**
     * Exception file name
     */
    @NotNull(message = "Exception file name cannot be null")
    private String exceptionFileName;

    /**
     * Exception method name
     */
    @NotNull(message = "Exception method name cannot be null")
    private String exceptionMethodName;

    /**
     * Exception line number
     */
    @NotNull(message = "Exception line number cannot be null")
    private Integer exceptionLineNumber;

    /**
     * Exception stack trace
     */
    @NotNull(message = "Exception stack trace cannot be null")
    private String exceptionStackTrace;

    /**
     * Root cause message
     */
    @NotNull(message = "Root cause message cannot be null")
    private String exceptionRootCauseMessage;

    /**
     * Exception message
     */
    @NotNull(message = "Exception message cannot be null")
    private String exceptionMessage;

}
