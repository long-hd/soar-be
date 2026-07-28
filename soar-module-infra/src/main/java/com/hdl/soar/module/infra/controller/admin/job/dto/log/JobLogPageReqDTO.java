package com.hdl.soar.module.infra.controller.admin.job.dto.log;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin Backend - Job Log Page Request DTO")
public class JobLogPageReqDTO extends PageParam {

    @Schema(description = "Job ID", example = "50")
    private Long jobId;

    @Schema(description = "Handler bean name (fuzzy match)", example = "clean")
    private String handlerName;

    @Schema(description = "Status: 0=RUNNING, 1=SUCCESS, 2=FAILURE", example = "1")
    private Integer status;

    @Schema(description = "Start time range")
    private Instant[] beginTime;

}