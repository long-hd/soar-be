package com.hdl.soar.module.infra.controller.admin.file.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - File Presigned URL Response DTO")
public class FilePresignedUrlRespDTO {

    @Schema(description = "File config ID", example = "11")
    private Long configId;

    @Schema(description = "Relative path (key)")
    private String path;

    @Schema(description = "Presigned upload URL (PUT)")
    private String uploadUrl;

    @Schema(description = "Final access URL after upload")
    private String url;

}