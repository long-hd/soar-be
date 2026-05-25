package com.hdl.soar.module.system.service.permission;

import com.hdl.soar.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;

import java.util.List;

/**
 * Permission Service Interface
 * <p>
 * Provides permission management for user-role, role-menu, and role-department associations
 */
public interface PermissionService {

    /**
     * Check whether the user has any of the given permissions (at least one is sufficient)
     *
     * @param userId      User ID
     * @param permissions  Permissions
     * @return Whether the user has permission
     */
    boolean hasAnyPermissions(Long userId, String... permissions);

    /**
     * Check whether the user has any of the given roles (at least one is sufficient)
     *
     * @param roles Roles array
     * @return Whether the user has role(s)
     */
    boolean hasAnyRoles(Long userId, String... roles);

    /**
     * Get the department data permission of the logged-in user
     *
     * @param userId User ID
     * @return Department data permission
     */
    DeptDataPermissionRespDTO getDeptDataPermission(Long userId);

}
