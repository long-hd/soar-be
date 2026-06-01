package com.hdl.soar.module.system.controller.admin.logger.dto.operatelog;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@ExcelIgnoreUnannotated
@Schema(description = "Admin Backend - Operate Log Response DTO")
public class OperateLogRespDTO {

    @Schema(description = "Log ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Log ID")
    private Long id;

    @Schema(description = "Trace ID", example = "89aca178-a370-411c-ae02-3f0d672be4ab")
    private String traceId;

    @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("User ID")
    private Long userId;

    @Schema(description = "User type", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer userType;

    @Schema(description = "Module", requiredMode = Schema.RequiredMode.REQUIRED, example = "System User")
    @ExcelProperty("Module")
    private String module;

    @Schema(description = "Operation name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Create User")
    @ExcelProperty("Operation Name")
    private String name;

    @Schema(description = "Business ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Business ID")
    private Long bizId;

    @Schema(description = "Action content", example = "Created user [Long]")
    @ExcelProperty("Content")
    private String content;

    @Schema(description = "Extra fields (JSON)", example = "{}")
    private String extra;

    @Schema(description = "Request method", example = "POST")
    @ExcelProperty("Request Method")
    private String requestMethod;

    @Schema(description = "Request URL", example = "/system/user/create")
    @ExcelProperty("Request URL")
    private String requestUrl;

    @Schema(description = "User IP", example = "127.0.0.1")
    @ExcelProperty("User IP")
    private String userIp;

    @Schema(description = "User Agent")
    private String userAgent;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Creation Time")
    private Instant createTime;

}