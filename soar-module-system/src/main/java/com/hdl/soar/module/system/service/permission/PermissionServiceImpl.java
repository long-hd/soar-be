package com.hdl.soar.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.hdl.soar.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import com.hdl.soar.framework.common.util.collection.CollectionUtils;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import com.hdl.soar.module.system.dal.entity.permission.RoleMenuPO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import com.hdl.soar.module.system.dal.postgres.permission.RoleMenuRepository;
import com.hdl.soar.module.system.dal.redis.RedisKeyConstants;
import com.hdl.soar.module.system.enums.permission.DataScopeEnum;
import com.hdl.soar.module.system.service.dept.DeptService;
import com.hdl.soar.module.system.service.user.AdminUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.*;

/**
 * Permission Service implementation class
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {

    AdminUserService userService;
    RoleService roleService;
    MenuService menuService;
    DeptService deptService;

    RoleMenuRepository roleMenuRepository;

    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        // If empty, it means there is no permission restriction, so access is allowed
        if (ArrayUtil.isEmpty(permissions)) {
            return true;
        }

        // Get the currently logged-in user's roles. If empty, the user has no permission
        List<RolePO> roles = roleService.getEnableRolesByUserIdFromCache(userId);
        if (CollUtil.isEmpty(roles)) {
            return false;
        }

        // Case 1: iterate through each permission; if any one matches, the user has permission
        for (String permission : permissions) {
            if (hasAnyPermission(roles, permission)) {
                return true;
            }
        }

        // Case 2: if the user is a super admin, also grant permission
        return roleService.hasAnySuperAdmin(convertSet(roles, RolePO::getId));
    }

    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        // If empty, it means permission is already granted
        if (ArrayUtil.isEmpty(roles)) {
            return true;
        }

        // Get roles of the currently logged-in user. If empty, no permission is granted
        List<RolePO> roleList = roleService.getEnableRolesByUserIdFromCache(userId);
        if (CollUtil.isEmpty(roleList)) {
            return false;
        }

        // Check whether the user has any of the required roles
        Set<String> userRoles = convertSet(roleList, RolePO::getCode);
        return CollUtil.containsAny(userRoles, Sets.newHashSet(roles));
    }

    @Override
    // @DataPermission(enable = false) // Disable data permission; otherwise, it may cause recursive data permission fetching issues
    public DeptDataPermissionRespDTO getDeptDataPermission(Long userId) {
        // Get the user's roles
        List<RolePO> roles = roleService.getEnableRolesByUserIdFromCache(userId);

        // If no roles exist, the user can only view their own data
        DeptDataPermissionRespDTO result = new DeptDataPermissionRespDTO();
        if (CollUtil.isEmpty(roles)) {
            result.setSelf(true);
            return result;
        }

        // Get the user's department ID using Guava Suppliers for lazy evaluation,
        // ensuring the DB query is executed only once when needed
        Supplier<Long> userDeptId = Suppliers.memoize(() -> userService.getUser(userId).getDeptId());

        // Iterate through each role to calculate data permissions
        for (RolePO role : roles) {
            // Skip if data scope is not defined
            if (role.getDataScope() == null) {
                continue;
            }

            // Case 1: ALL
            if (DataScopeEnum.ALL.equals(role.getDataScope())) {
                result.setAll(true);
                continue;
            }

            // Case 2: DEPT_CUSTOM
            if (DataScopeEnum.DEPT_CUSTOM.equals(role.getDataScope())) {
                CollUtil.addAll(result.getDeptIds(), role.getDataScopeDeptIds());
                // Ensure the user's own department is included to avoid potential issues
                // For example, login queries based on t_user.username may be filtered out by dept_id
                CollectionUtils.addIfNotNull(result.getDeptIds(), userDeptId.get());
                continue;
            }

            // Case 3: DEPT_ONLY
            if (DataScopeEnum.DEPT_ONLY.equals(role.getDataScope())) {
                CollectionUtils.addIfNotNull(result.getDeptIds(), userDeptId.get());
                continue;
            }

            // Case 4: DEPT_AND_CHILD
            if (DataScopeEnum.DEPT_AND_CHILD.equals(role.getDataScope())) {
                Long deptId = userDeptId.get();
                // If user has no department, skip to avoid null key issues in cache method
                if (deptId == null) {
                    continue;
                }
                CollUtil.addAll(result.getDeptIds(), deptService.getChildDeptIdsFromCache(deptId));
                // Include the user's own department
                result.getDeptIds().add(deptId);
                continue;
            }

            // Case 5: SELF
            if (DataScopeEnum.SELF.equals(role.getDataScope())) {
                result.setSelf(true);
                continue;
            }

            // Unknown case: log error
            log.error("[getDeptDataPermission][LoginUser({}) role({}) cannot be processed]",
                    userId, JsonUtils.toJsonString(result));
        }

        return result;
    }

    // =================== Role Menu

    @Override
    public Set<Long> getMenuIdsByRoleIds(Collection<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptySet();
        }

        // If the user is a super admin, return all menu IDs
        if(roleService.hasAnySuperAdmin(roleIds)){
            return convertSet(menuService.getMenuList(), MenuPO::getId);
        }

        // Otherwise, return menu IDs associated with the given roles
        return roleMenuRepository.findAllByRoleIdIn(roleIds);
    }

    // =================== Helper

    /**
     * Get the self proxy object to ensure AOP takes effect
     *
     * @return self instance
     */
    private PermissionServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    /**
     * Determine whether the specified roles have the given permission.
     *
     * @param roles array of specified roles
     * @param permission permission identifier
     * @return whether the permission is granted
     */
    private boolean hasAnyPermission(List<RolePO> roles, String permission) {
        List<Long> menuIds = menuService.getMenuIdsByPermissionFromCache(permission);
        // Strict mode: if no corresponding Menu is found for the permission,
        // it is also considered as no permission.
        if (CollUtil.isEmpty(menuIds)) {
            return false;
        }

        // Check whether the permission is granted
        Set<Long> roleIds = convertSet(roles, RolePO::getId);
        for (Long menuId : menuIds) {
            // Get the set of role IDs that have access to this menu
            Set<Long> menuRoleIds = getSelf().getRoleIdsByMenuIdFromCache(menuId);
            // If there is any intersection, the permission is granted
            if (CollUtil.containsAny(menuRoleIds, roleIds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the set of role IDs associated with the given menu IDs.
     *
     * @param menuId menu identifier
     * @return set of role IDs
     */
    @Cacheable(value = RedisKeyConstants.MENU_ROLE_ID_LIST, key = "#menuId")
    public Set<Long> getRoleIdsByMenuIdFromCache(Long menuId) {
        return convertSet(roleMenuRepository.findAllByMenuId(menuId), RoleMenuPO::getRoleId);
    }

}
