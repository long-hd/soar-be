package com.hdl.soar.module.system.controller.admin.permission.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Menu Create/Update Request DTO")
public class MenuSaveReqDTO {

    @Schema(description = "Menu ID", example = "1024")
    private Long id;

    @Schema(description = "Menu name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Soar")
    @NotBlank(message = "Menu name cannot be empty")
    @Size(max = 50, message = "Menu name length cannot exceed 50 characters")
    private String name;

    @Schema(description = "Permission identifier, only required when the menu type is button", example = "sys:menu:add")
    @Size(max = 100)
    private String permission;

    @Schema(description = "Type, see MenuTypeEnum enum class", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Menu type cannot be empty")
    private Integer type;

    @Schema(description = "Display order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Display order cannot be empty")
    private Integer sort;

    @Schema(description = "Parent menu ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Parent menu ID cannot be empty")
    private Long parentId;

    @Schema(description = "Route path, only required when the menu type is menu or directory", example = "post")
    @Size(max = 200, message = "Route path cannot exceed 200 characters")
    private String path;

    @Schema(description = "Menu icon, only required when the menu type is menu or directory", example = "/menu/list")
    private String icon;

    @Schema(description = "Component path, only required when the menu type is menu", example = "system/post/index")
    @Size(max = 200, message = "Component path cannot exceed 255 characters")
    private String component;

    @Schema(description = "Component name", example = "SystemUser")
    private String componentName;

    @Schema(description = "Status, see CommonStatusEnum enum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Status cannot be empty")
    private Integer status;

    @Schema(description = "Whether visible", example = "false")
    private Boolean visible;

    @Schema(description = "Whether to cache", example = "false")
    private Boolean keepAlive;

    @Schema(description = "Whether to always show", example = "false")
    private Boolean alwaysShow;

}
