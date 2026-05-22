package com.hdl.soar.module.system.api.logger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Login Log Creation Request DTO
 */
@Data
public class LoginLogCreateReqDTO {
    /**
     * Log type
     */
    @NotNull(message = "Log type cannot be null")
    private Integer logType;

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
    @NotNull(message = "User type cannot be null")
    private Integer userType;

    /**
     * Username
     *
     * No longer required, because for Member social login, username may not be available (mobile used instead)
     */
    private String username;

    /**
     * Login result
     */
    @NotNull(message = "Login result cannot be null")
    private Integer result;

    /**
     * User IP
     */
    @NotBlank(message = "User IP cannot be empty")
    private String userIp;

    /**
     * Browser User-Agent
     *
     * Optional, because during Job-based forced logout, User-Agent cannot be provided
     */
    private String userAgent;
}
