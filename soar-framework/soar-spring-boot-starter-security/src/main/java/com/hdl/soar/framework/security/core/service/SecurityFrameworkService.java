package com.hdl.soar.framework.security.core.service;

/**
 * Security framework service interface that defines permission-related validation operations.
 */
public interface SecurityFrameworkService {

    /**
     * Check whether the user has a specific permission.
     *
     * @param permission the permission string
     * @return whether the user has the permission
     */
    boolean hasPermission(String permission);

    /**
     * Check whether the user has any of the given permissions.
     *
     * @param permissions the permission strings
     * @return whether the user has at least one of the permissions
     */
    boolean hasAnyPermissions(String... permissions);

    /**
     * Check whether the user has a specific role.
     *
     * Note: Roles use the code identifier from SysRoleDO.
     *
     * @param role the role
     * @return whether the user has the role
     */
    boolean hasRole(String role);

    /**
     * Check whether the user has any of the given roles.
     *
     * @param roles array of roles
     * @return whether the user has at least one of the roles
     */
    boolean hasAnyRoles(String... roles);

    /**
     * Check whether the user has a specific scope.
     *
     * @param scope the scope
     * @return whether the user has the scope
     */
    boolean hasScope(String scope);

    /**
     * Check whether the user has any of the given scopes.
     *
     * @param scope array of scopes
     * @return whether the user has at least one of the scopes
     */
    boolean hasAnyScopes(String... scope);

}
