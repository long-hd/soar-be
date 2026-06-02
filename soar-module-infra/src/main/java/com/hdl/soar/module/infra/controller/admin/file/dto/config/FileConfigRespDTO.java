package com.hdl.soar.module.infra.controller.admin.file.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Schema(description = "Admin Backend - File Config Response DTO")
public class FileConfigRespDTO {

    @Schema(description = "Config ID", example = "1024")
    private Long id;

    @Schema(description = "Config name", example = "Local Dev Storage")
    private String name;

    @Schema(description = "Storage type: 1=DB, 10=LOCAL, 20=S3", example = "10")
    private Integer storage;

    @Schema(description = "Whether this is the master config", example = "true")
    private Boolean master;

    @Schema(description = "Storage-specific config map")
    private Map<String, Object> config;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Creation time")
    private Instant createTime;

}