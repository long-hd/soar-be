package com.hdl.soar.module.infra.controller.admin.logger.dto.apiaccesslog;

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
@Schema(description = "Admin Backend - API Access Log Response DTO")
public class ApiAccessLogRespDTO {

    @Schema(description = "Log ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Log ID")
    private Long id;

    @Schema(description = "Trace ID", example = "66600cb6-7852-11eb-9439-0242ac130002")
    @ExcelProperty("Trace ID")
    private String traceId;

    @Schema(description = "User ID", example = "666")
    @ExcelProperty("User ID")
    private Long userId;

    @Schema(description = "User type", example = "2")
    private Integer userType;

    @Schema(description = "Application name", example = "soar-server")
    @ExcelProperty("Application Name")
    private String applicationName;

    @Schema(description = "Request method", example = "GET")
    @ExcelProperty("Request Method")
    private String requestMethod;

    @Schema(description = "Request URL", example = "/system/user/page")
    @ExcelProperty("Request URL")
    private String requestUrl;

    @Schema(description = "Request parameters")
    private String requestParams;

    @Schema(description = "Response body")
    private String responseBody;

    @Schema(description = "User IP", example = "127.0.0.1")
    @ExcelProperty("User IP")
    private String userIp;

    @Schema(description = "User Agent", example = "Mozilla/5.0")
    private String userAgent;

    @Schema(description = "Operate module", example = "User Management")
    @ExcelProperty("Operate Module")
    private String operateModule;

    @Schema(description = "Operate name", example = "Create user")
    @ExcelProperty("Operate Name")
    private String operateName;

    @Schema(description = "Operate type", example = "1")
    @ExcelProperty(value = "Operate Type", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.OPERATE_TYPE)
    private Integer operateType;

    @Schema(description = "Begin time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Begin Time")
    private Instant beginTime;

    @Schema(description = "End time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("End Time")
    private Instant endTime;

    @Schema(description = "Duration (ms)", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("Duration (ms)")
    private Integer duration;

    @Schema(description = "Result code", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("Result Code")
    private Integer resultCode;

    @Schema(description = "Result message", example = "")
    private String resultMsg;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createTime;

}
