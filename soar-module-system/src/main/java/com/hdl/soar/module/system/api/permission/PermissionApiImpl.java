package com.hdl.soar.module.system.api.permission;

import com.hdl.soar.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import com.hdl.soar.module.system.service.permission.PermissionService;
import com.hdl.soar.module.system.service.permission.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * Permission API Implementation Class
 *
 * @author YuDao Source Code
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionApiImpl implements PermissionApi {

    RoleService roleService;
    PermissionService permissionService;

    @Override
    public Set<Long> getUserIdsByRoleIds(Collection<Long> roleIds) {
        return roleService.getUserIdsByRoleIds(roleIds);
    }

    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        return permissionService.hasAnyPermissions(userId, permissions);
    }

    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        return permissionService.hasAnyRoles(userId, roles);
    }

    @Override
    public DeptDataPermissionRespDTO getDeptDataPermission(Long userId) {
        return permissionService.getDeptDataPermission(userId);
    }
}
