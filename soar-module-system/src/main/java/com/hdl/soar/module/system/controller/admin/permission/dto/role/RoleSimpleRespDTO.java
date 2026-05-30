package com.hdl.soar.module.system.controller.admin.permission.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Role Simple Response DTO")
public class RoleSimpleRespDTO {

    @Schema(description = "Role ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "Role name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Administrator")
    private String name;

    @Schema(description = "Role code", requiredMode = Schema.RequiredMode.REQUIRED, example = "ADMIN")
    private String code;

}