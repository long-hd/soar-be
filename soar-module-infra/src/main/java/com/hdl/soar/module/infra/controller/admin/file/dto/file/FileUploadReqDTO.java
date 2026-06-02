package com.hdl.soar.module.infra.controller.admin.file.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Admin Backend - File Upload Request DTO")
public class FileUploadReqDTO {

    @Schema(description = "File", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "File cannot be null")
    private MultipartFile file;

    @Schema(description = "Sub-directory", example = "avatars")
    private String directory;

}