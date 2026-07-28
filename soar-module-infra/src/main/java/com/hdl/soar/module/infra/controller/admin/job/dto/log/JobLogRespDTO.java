package com.hdl.soar.module.infra.controller.admin.job.dto.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin Backend - Job Log Response DTO")
public class JobLogRespDTO {

    @Schema(description = "Log ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Job ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Long jobId;

    @Schema(description = "Handler bean name", requiredMode = Schema.RequiredMode.REQUIRED, example = "jobLogCleanJob")
    private String handlerName;

    @Schema(description = "Handler parameter", example = "30")
    private String handlerParam;

    @Schema(description = "Execution attempt (>1 = retry)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer executeIndex;

    @Schema(description = "Start time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant beginTime;

    @Schema(description = "End time")
    private Instant endTime;

    @Schema(description = "Duration in milliseconds", example = "1200")
    private Integer duration;

    @Schema(description = "Status: 0=RUNNING, 1=SUCCESS, 2=FAILURE", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "Result or error message")
    private String result;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createTime;

}