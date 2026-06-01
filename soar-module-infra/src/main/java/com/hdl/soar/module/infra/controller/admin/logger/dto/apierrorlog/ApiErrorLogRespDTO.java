package com.hdl.soar.module.infra.controller.admin.logger.dto.apierrorlog;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.hdl.soar.framework.excel.core.annotations.DictFormat;
import com.hdl.soar.framework.excel.core.convert.DictConvert;
import com.hdl.soar.module.infra.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@ExcelIgnoreUnannotated
@Schema(description = "Admin Backend - API Error Log Response DTO")
public class ApiErrorLogRespDTO {

    @Schema(description = "Log ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Log ID")
    private Long id;

    @Schema(description = "Trace ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "66600cb6-7852-11eb-9439-0242ac130002")
    @ExcelProperty("Trace ID")
    private String traceId;

    @Schema(description = "User ID", example = "666")
    @ExcelProperty("User ID")
    private Long userId;

    @Schema(description = "User type", example = "2")
    private Integer userType;

    @Schema(description = "Application name", requiredMode = Schema.RequiredMode.REQUIRED, example = "soar-server")
    @ExcelProperty("Application Name")
    private String applicationName;

    @Schema(description = "Request method", requiredMode = Schema.RequiredMode.REQUIRED, example = "GET")
    @ExcelProperty("Request Method")
    private String requestMethod;

    @Schema(description = "Request URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "/system/user/page")
    @ExcelProperty("Request URL")
    private String requestUrl;

    @Schema(description = "Request parameters")
    private String requestParams;

    @Schema(description = "User IP", requiredMode = Schema.RequiredMode.REQUIRED, example = "127.0.0.1")
    @ExcelProperty("User IP")
    private String userIp;

    @Schema(description = "User Agent", example = "Mozilla/5.0")
    private String userAgent;

    // ========== Exception ==========

    @Schema(description = "Exception time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Exception Time")
    private Instant exceptionTime;

    @Schema(description = "Exception name", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Exception Name")
    private String exceptionName;

    @Schema(description = "Exception message")
    private String exceptionMessage;

    @Schema(description = "Root cause message")
    private String exceptionRootCauseMessage;

    @Schema(description = "Exception stack trace")
    private String exceptionStackTrace;

    @Schema(description = "Exception class name")
    private String exceptionClassName;

    @Schema(description = "Exception file name")
    private String exceptionFileName;

    @Schema(description = "Exception method name")
    private String exceptionMethodName;

    @Schema(description = "Exception line number")
    private Integer exceptionLineNumber;

    // ========== Processing ==========

    @Schema(description = "Processing status", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "Process Status", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.API_ERROR_LOG_PROCESS_STATUS)
    private Integer processStatus;

    @Schema(description = "Process time")
    private Instant processTime;

    @Schema(description = "Process user ID", example = "233")
    private Long processUserId;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Creation Time")
    private Instant createTime;

}
