package com.hdl.soar.module.system.controller.admin.permission;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.system.controller.admin.permission.dto.permission.RoleAssignDataScopeReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.permission.RoleAssignMenuReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.permission.UserAssignRoleReqDTO;
import com.hdl.soar.module.system.service.permission.PermissionService;
import com.hdl.soar.module.system.service.permission.RoleService;
import com.hdl.soar.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - Permission")
@Validated
@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {

    RoleService roleService;
    PermissionService permissionService;
    AdminUserService adminUserService;

    @PostMapping("/assign-role-data-scope")
    @Operation(summary = "Assign data scope to a role")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-data-scope')")
    public CommonResult<Boolean> assignRoleDataScope(@Valid @RequestBody RoleAssignDataScopeReqDTO reqDTO) {
        roleService.updateRoleDataScope(reqDTO.getRoleId(), reqDTO.getDataScope(), reqDTO.getDataScopeDeptIds());
        return success(true);
    }

    @GetMapping("/list-user-roles")
    @Operation(summary = "Get the list of role IDs assigned to an administrator")
    @Parameter(name = "userId", description = "User ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:permission:assign-user-role')")
    public CommonResult<Set<Long>> getUserRoleIds(@RequestParam("userId") Long userId) {
        return success(roleService.getRoleIdsByUserId(userId));
    }

    @PutMapping("/assign-user-role")
    @Operation(summary = "Assign a role to a user")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-user-role')")
    public CommonResult<Boolean> assignUserRole(@Valid @RequestBody UserAssignRoleReqDTO reqDTO) {
        adminUserService.assignRoles(reqDTO.getUserId(), reqDTO.getRoleIds());
        return success(true);
    }

    @GetMapping("/list-role-menus")
    @Operation(summary = "Get the list of menu IDs assigned to a role")
    @Parameter(name = "roleId", description = "Role ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-menu')")
    public CommonResult<Set<Long>> getRoleMenuList(@RequestParam("roleId") Long roleId) {
        return success(permissionService.getMenuIdsByRoleId(roleId));
    }

    @PostMapping("/assign-role-menu")
    @Operation(summary = "Assign menus to a role")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-menu')")
    public CommonResult<Boolean> assignRoleMenu(@Valid @RequestBody RoleAssignMenuReqDTO reqDTO) {
        permissionService.assignRoleMenu(reqDTO.getRoleId(), reqDTO.getMenuIds());
        return success(true);
    }

}
