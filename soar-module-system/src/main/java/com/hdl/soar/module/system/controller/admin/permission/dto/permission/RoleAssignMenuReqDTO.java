package com.hdl.soar.module.system.controller.admin.permission.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
@Schema(description = "Admin - Assign role menu Request DTO")
public class RoleAssignMenuReqDTO {

    @Schema(description = "Role ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Role ID must not be null")
    private Long roleId;

    @Schema(description = "Menu ID list (empty = clear all)", example = "[1, 3, 5]")
    private Set<Long> menuIds = Collections.emptySet();  // defensive default

}