package com.hdl.soar.module.infra.controller.admin.file.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin Backend - File Response DTO")
public class FileRespDTO {

    @Schema(description = "File ID", example = "1024")
    private Long id;

    @Schema(description = "File config ID", example = "11")
    private Long configId;

    @Schema(description = "File name")
    private String name;

    @Schema(description = "Relative path")
    private String path;

    @Schema(description = "Full access URL")
    private String url;

    @Schema(description = "Content type")
    private String type;

    @Schema(description = "Size in bytes", example = "2048")
    private Integer size;

    @Schema(description = "Creation time")
    private Instant createTime;

}