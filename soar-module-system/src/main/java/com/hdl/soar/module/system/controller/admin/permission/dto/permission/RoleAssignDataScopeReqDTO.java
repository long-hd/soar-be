package com.hdl.soar.module.system.controller.admin.permission.dto.permission;

import com.hdl.soar.framework.common.validation.InEnum;
import com.hdl.soar.module.system.enums.permission.DataScopeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Admin - Role assign data scope Request DTO")
public class RoleAssignDataScopeReqDTO {

    @Schema(description = "Role ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Role ID must not be null")
    private Long roleId;

    @Schema(description = "Data scope type", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Data scope must not be null")
    @InEnum(DataScopeEnum.class)
    private Integer dataScope;

    @Schema(description = "Custom dept IDs (only for DEPT_CUSTOM)")
    private Set<Long> dataScopeDeptIds;

}
