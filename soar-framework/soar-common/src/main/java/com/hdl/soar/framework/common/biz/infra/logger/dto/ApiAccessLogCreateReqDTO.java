package com.hdl.soar.framework.common.biz.infra.logger.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

/**
 * API access log
 */
@Data
public class ApiAccessLogCreateReqDTO {

    /**
     * Trace ID (distributed tracing identifier)
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
    private String requestParams;
    /**
     * Response result
     */
    private String responseBody;
    /**
     * User IP address
     */
    @NotNull(message = "IP cannot be null")
    private String userIp;
    /**
     * Browser User-Agent
     */
    @NotNull(message = "User-Agent cannot be null")
    private String userAgent;

    /**
     * Operation module
     */
    private String operateModule;
    /**
     * Operation name
     */
    private String operateName;
    /**
     * Operation category
     *
     * Enum, see OperateTypeEnum
     */
    private Integer operateType;

    /**
     * Start request time
     */
    @NotNull(message = "Start request time cannot be null")
    private Instant beginTime;
    /**
     * End request time
     */
    @NotNull(message = "End request time cannot be null")
    private Instant endTime;

    /**
     * Execution duration, in milliseconds
     */
    @NotNull(message = "Execution duration cannot be null")
    private Integer duration;
    /**
     * Result code
     */
    @NotNull(message = "Error code cannot be null")
    private Integer resultCode;
    /**
     * Result message
     */
    private String resultMsg;

}
