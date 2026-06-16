package com.hdl.soar.module.system.service.permission;

import com.hdl.soar.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    // ================== Role Menu

    /**
     * Get the set of menu IDs owned by roles
     *
     * @param roleIds role ID collection
     * @return set of menu IDs
     */
    Set<Long> getMenuIdsByRoleIds(Collection<Long> roleIds);

    /**
     * Get the set of menu IDs assigned to a single role.
     * Used by the admin assign-role-menu UI to populate the picker with current state.
     *
     * @param roleId role ID
     * @return set of menu IDs (empty if none assigned)
     */
    Set<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * Assign menus to a role. Computes diff against current assignment:
     *  - menus in `menuIds` but not in DB → inserted
     *  - menus in DB but not in `menuIds` → deleted
     *  - menus in both → untouched (idempotent)
     *
     * Pass an empty set to revoke all menus from the role.
     *
     * @param roleId role ID
     * @param menuIds target menu IDs (may be empty)
     */
    void assignRoleMenu(Long roleId, Set<Long> menuIds);

}
