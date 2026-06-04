package com.hdl.soar.module.system.service.permission;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RolePageReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RoleSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import com.hdl.soar.module.system.enums.permission.RoleTypeEnum;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Role Service interface
 */
public interface RoleService {

    /**
     * Create a role
     *
     * @param createReqDTO role creation information
     * @param type role type
     * @return role ID
     */
    Long createRole(@Valid RoleSaveReqDTO createReqDTO, RoleTypeEnum type);

    /**
     * Update role
     *
     * @param updateReqDTO role update information
     */
    void updateRole(@Valid RoleSaveReqDTO updateReqDTO);

    /**
     * Delete role
     *
     * @param id role ID
     */
    void deleteRole(Long id);

    /**
     * Batch delete roles
     *
     * @param ids array of role IDs
     */
    void deleteRoleList(List<Long> ids);

    /**
     * Get role
     *
     * @param id role ID
     * @return role
     */
    RolePO getRole(Long id);

    /**
     * Get role page
     *
     * @param pageReqDTO role page query
     * @return paginated role result
     */
    PageResult<RolePO> getRolePage(RolePageReqDTO pageReqDTO);

    /**
     * Get role list
     *
     * @param status status filters
     * @return role list
     */
    List<RolePO> getRoleListByStatus(CommonStatusEnum status);

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
     * Get role list by ID in given IDs
     *
     * @param roleIds role ID collection
     * @return role list
     */
    List<RolePO> getRolesByIdIn(Collection<Long> roleIds);

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

    /**
     * Update the data scope of a role.
     *
     * @param roleId            role ID
     * @param dataScope         data scope type (see DataScopeEnum)
     * @param dataScopeDeptIds  custom dept IDs (only for DEPT_CUSTOM)
     */
    void updateRoleDataScope(Long roleId, Integer dataScope, Set<Long> dataScopeDeptIds);

}
