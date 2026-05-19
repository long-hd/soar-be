package com.hdl.soar.framework.common.biz.system.permission;

import com.hdl.soar.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;

/**
 * Permission API interface
 */
public interface PermissionCommonApi {

    /**
     * Check whether the user has any of the given permissions.
     *
     * @param userId the user ID
     * @param permissions the permission strings
     * @return whether the user has at least one of the permissions
     */
    boolean hasAnyPermissions(Long userId, String... permissions);

    /**
     * Check whether the user has any of the given roles.
     *
     * @param userId the user ID
     * @param roles array of roles
     * @return whether the user has at least one of the roles
     */
    boolean hasAnyRoles(Long userId, String... roles);

    /**
     * Get the department data permission of the logged-in user.
     *
     * @param userId the user ID
     * @return the department data permission
     */
    DeptDataPermissionRespDTO getDeptDataPermission(Long userId);

}
