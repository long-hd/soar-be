package com.hdl.soar.module.infra.controller.admin.file.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - File Create (metadata) Request DTO")
public class FileCreateReqDTO {

    @Schema(description = "File config ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "Config ID cannot be null")
    private Long configId;

    @Schema(description = "File name")
    private String name;

    @Schema(description = "Relative path", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Path cannot be null")
    private String path;

    @Schema(description = "Full access URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "URL cannot be null")
    private String url;

    @Schema(description = "Content type")
    private String type;

    @Schema(description = "Size in bytes")
    private Integer size;

}