package com.hdl.soar.module.infra.controller.admin.job.dto.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin Backend - Scheduled Job Response DTO")
public class JobRespDTO {

    @Schema(description = "Job ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Job name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Clean job logs")
    private String name;

    @Schema(description = "Status: 0=INIT, 1=NORMAL, 2=STOP", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "Handler bean name", requiredMode = Schema.RequiredMode.REQUIRED, example = "jobLogCleanJob")
    private String handlerName;

    @Schema(description = "Handler parameter", example = "30")
    private String handlerParam;

    @Schema(description = "CRON expression", requiredMode = Schema.RequiredMode.REQUIRED, example = "0 0 2 * * ?")
    private String cronExpression;

    @Schema(description = "Retry count", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer retryCount;

    @Schema(description = "Retry interval in milliseconds", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer retryInterval;

    @Schema(description = "Alerting threshold in milliseconds", example = "60000")
    private Integer monitorTimeout;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createTime;

}