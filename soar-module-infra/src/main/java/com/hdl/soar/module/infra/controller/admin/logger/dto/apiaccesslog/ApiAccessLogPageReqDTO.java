package com.hdl.soar.module.infra.controller.admin.logger.dto.apiaccesslog;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin Backend - API Access Log Page Request DTO")
public class ApiAccessLogPageReqDTO extends PageParam {

    @Schema(description = "User ID", example = "666")
    private Long userId;

    @Schema(description = "User type", example = "2")
    private Integer userType;

    @Schema(description = "Application name", example = "soar-server")
    private String applicationName;

    @Schema(description = "Request URL (fuzzy match)", example = "/system/user/page")
    private String requestUrl;

    @Schema(description = "Begin time range", example = "[2024-01-01T00:00:00Z,2024-01-31T23:59:59Z]")
    private Instant[] beginTime;

    @Schema(description = "Duration >= (milliseconds)", example = "100")
    private Integer duration;

    @Schema(description = "Result code", example = "0")
    private Integer resultCode;

}
