package com.hdl.soar.framework.common.biz.system.logger.dto;

import com.hdl.soar.framework.common.enums.UserTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OperateLogCreateReqDTO {

    /**
     * Trace ID for correlating with access/error logs.
     */
    private String traceId;

    /**
     * User ID who performed the operation.
     */
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    /**
     * User type (admin, member, etc).
     *
     * <p>See {@link UserTypeEnum}.
     */
    @NotNull(message = "User type cannot be null")
    private UserTypeEnum userType;

    /**
     * Operation module (e.g., "System User", "System Role").
     */
    @NotEmpty(message = "Module cannot be empty")
    private String module;

    /**
     * Operation name (e.g., "Create User", "Update Role").
     */
    @NotEmpty(message = "Name cannot be empty")
    private String name;

    /**
     * Business entity ID (e.g., user ID, role ID).
     */
    @NotNull(message = "Business ID cannot be null")
    private Long bizId;

    /**
     * Human-readable action description.
     * <p>
     * Example: "Created user [Long]"
     */
    private String content;

    /**
     * Extra fields in JSON format for complex business scenarios.
     */
    private String extra;

    // ========== Request context ==========

    private String requestMethod;
    private String requestUrl;
    private String userIp;
    private String userAgent;

}
