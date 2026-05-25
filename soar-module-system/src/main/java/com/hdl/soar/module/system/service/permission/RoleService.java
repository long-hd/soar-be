package com.hdl.soar.module.system.service.permission;

import com.hdl.soar.module.system.dal.entity.permission.RolePO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Role Service interface
 */
public interface RoleService {

    /**
     * Get the list of user IDs associated with multiple roles
     *
     * @param roleIds collection of role IDs
     * @return collection of user IDs
     */
    Set<Long> getUserIdsByRoleIds(Collection<Long> roleIds);

    /**
     * Get the set of role IDs owned by the user
     *
     * @param userId User ID
     * @return Set of role IDs
     */
    Set<Long> getRoleIdsByUserId(Long userId);

    /**
     * Get the set of role IDs owned by the user from the cache
     *
     * @param userId User ID
     * @return Set of role IDs
     */
    Set<Long> getRoleIdsByUserIdFromCache(Long userId);

    /**
     * Get the role from the cache
     *
     * @param id Role ID
     * @return Role
     */
    RolePO getRoleFromCache(Long id);

    /**
     * Get the list of roles from the cache
     *
     * @param roleIds Role ID collection
     * @return Role list
     */
    List<RolePO> getRolesFromCache(Collection<Long> roleIds);


    /**
     * Get the roles owned by the user, only including enabled roles
     *
     * @param userId User ID
     * @return List of roles owned by the user
     */
    List<RolePO> getEnableRolesByUserIdFromCache(Long userId);

    /**
     * Determine whether the given collection of role IDs contains any super admin.
     *
     * @param roleIds collection of role IDs
     * @return whether a super admin is included
     */
    boolean hasAnySuperAdmin(Collection<Long> roleIds);

}
