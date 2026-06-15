package com.hdl.soar.module.system.controller.admin.permission.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin Backend - Menu Response DTO")
public class MenuRespDTO {

    @Schema(description = "Menu ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Tab key for flat URL dispatch (Soar-specific)", example = "system-user")
    @Size(max = 100)
    private String tabKey;

    @Schema(description = "Menu name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Soar")
    @NotBlank(message = "Menu name cannot be empty")
    @Size(max = 50, message = "Menu name length cannot exceed 50 characters")
    private String name;

    @Schema(description = "Permission identifier, only required for button type menus", example = "sys:menu:add")
    @Size(max = 100)
    private String permission;

    @Schema(description = "Type, see MenuTypeEnum enum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Menu type cannot be empty")
    private Integer type;

    @Schema(description = "Display order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Display order cannot be empty")
    private Integer sort;

    @Schema(description = "Parent menu ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Parent menu ID cannot be empty")
    private Long parentId;

    @Schema(description = "Route path, only required for menu or directory type", example = "post")
    @Size(max = 200, message = "Route path cannot exceed 200 characters")
    private String path;

    @Schema(description = "Menu icon, only required for menu or directory type", example = "/menu/list")
    private String icon;

    @Schema(description = "Component path, only required for menu type", example = "system/post/index")
    @Size(max = 200, message = "Component path cannot exceed 255 characters")
    private String component;

    @Schema(description = "Component name", example = "SystemUser")
    private String componentName;

    @Schema(description = "Status, see CommonStatusEnum enum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Status cannot be empty")
    private Integer status;

    @Schema(description = "Whether visible", example = "false")
    private Boolean visible;

    @Schema(description = "Whether cached", example = "false")
    private Boolean keepAlive;

    @Schema(description = "Whether always show", example = "false")
    private Boolean alwaysShow;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED, example = "timestamp")
    private Instant createTime;

}
