package com.hdl.soar.module.infra.controller.admin.config.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Config Create/Update Request DTO")
public class ConfigSaveReqDTO {

    @Schema(description = "Config ID (null for create)", example = "1024")
    private Long id;

    @Schema(description = "Config category", requiredMode = Schema.RequiredMode.REQUIRED, example = "biz")
    @NotBlank(message = "Category cannot be blank")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @Schema(description = "Config name", requiredMode = Schema.RequiredMode.REQUIRED, example = "User initial password")
    @NotBlank(message = "Config name cannot be blank")
    @Size(max = 100, message = "Config name must not exceed 100 characters")
    private String name;

    @Schema(description = "Config key", requiredMode = Schema.RequiredMode.REQUIRED, example = "system.user.init-password")
    @NotBlank(message = "Config key cannot be blank")
    @Size(max = 100, message = "Config key must not exceed 100 characters")
    private String key;

    @Schema(description = "Config value", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @Size(max = 500, message = "Config value must not exceed 500 characters")
    private String value;

    @Schema(description = "Visible to frontend", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "Visible flag cannot be null")
    private Boolean visible;

    @Schema(description = "Remark", example = "Default password for new users")
    private String remark;

}
