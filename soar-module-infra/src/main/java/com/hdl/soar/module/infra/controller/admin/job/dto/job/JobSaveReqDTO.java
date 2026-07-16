package com.hdl.soar.module.infra.controller.admin.job.dto.job;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Scheduled Job Create/Update Request DTO")
public class JobSaveReqDTO {

    @Schema(description = "Job ID (null for create)", example = "1024")
    private Long id;

    @Schema(description = "Job name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Clean job logs")
    @NotBlank(message = "Job name cannot be blank")
    private String name;

    @Schema(description = "Handler bean name", requiredMode = Schema.RequiredMode.REQUIRED, example = "jobLogCleanJob")
    @NotBlank(message = "Handler name cannot be blank")
    private String handlerName;

    @Schema(description = "Handler parameter", example = "30")
    private String handlerParam;

    @Schema(description = "CRON expression", requiredMode = Schema.RequiredMode.REQUIRED, example = "0 0 2 * * ?")
    @NotBlank(message = "CRON expression cannot be blank")
    private String cronExpression;

    @Schema(description = "Retry count; 0 = no retry", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "Retry count cannot be null")
    private Integer retryCount;

    @Schema(description = "Retry interval in milliseconds; 0 = no wait", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "Retry interval cannot be null")
    private Integer retryInterval;

    @Schema(description = "Alerting threshold in milliseconds; null = no monitoring", example = "60000")
    private Integer monitorTimeout;

}