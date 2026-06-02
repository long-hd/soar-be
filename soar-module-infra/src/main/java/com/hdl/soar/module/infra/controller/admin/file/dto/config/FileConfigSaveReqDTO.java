package com.hdl.soar.module.infra.controller.admin.file.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "Admin Backend - File Config Create/Update Request DTO")
public class FileConfigSaveReqDTO {

    @Schema(description = "Config ID (null for create)", example = "1024")
    private Long id;

    @Schema(description = "Config name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Local Dev Storage")
    @NotBlank(message = "Config name cannot be blank")
    private String name;

    @Schema(description = "Storage type: 1=DB, 10=LOCAL, 20=S3", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "Storage type cannot be null")
    private Integer storage;

    @Schema(description = "Remark", example = "Default storage for development")
    private String remark;

    /**
     * Storage-specific config. Shape depends on {@code storage}:
     * DB -> {domain?}; LOCAL -> {basePath, domain?}; S3 -> {endpoint, bucket, accessKey, accessSecret, ...}.
     * Parsed and validated against the matching {@code FileClientConfig} subtype in the service.
     */
    @Schema(description = "Storage-specific config map", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Config cannot be null")
    private Map<String, Object> config;

}