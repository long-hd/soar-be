package com.hdl.soar.module.system.controller.admin.permission.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Schema(description = "Admin Console - Assign User Roles Request DTO")
@Data
public class UserAssignRoleReqDTO {

    @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Schema(description = "List of Role IDs", example = "1,3,5")
    private Set<Long> roleIds = Collections.emptySet(); // Fallback default value

}
