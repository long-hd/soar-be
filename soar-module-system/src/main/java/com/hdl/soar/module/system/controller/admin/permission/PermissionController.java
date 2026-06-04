package com.hdl.soar.module.system.controller.admin.permission;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.system.controller.admin.permission.dto.permission.RoleAssignDataScopeReqDTO;
import com.hdl.soar.module.system.service.permission.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - Permission")
@Validated
@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {

    RoleService roleService;

    @PostMapping("/assign-role-data-scope")
    @Operation(summary = "Assign data scope to a role")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-data-scope')")
    public CommonResult<Boolean> assignRoleDataScope(@Valid @RequestBody RoleAssignDataScopeReqDTO reqDTO) {
        roleService.updateRoleDataScope(reqDTO.getRoleId(), reqDTO.getDataScope(), reqDTO.getDataScopeDeptIds());
        return success(true);
    }

}
