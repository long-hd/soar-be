package com.hdl.soar.framework.security.core.service;

import cn.hutool.core.collection.CollUtil;
import com.hdl.soar.framework.common.biz.system.permission.PermissionCommonApi;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import lombok.AllArgsConstructor;

import java.util.Arrays;

import static com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils.skipPermissionCheck;


@AllArgsConstructor
public class SecurityFrameworkServiceImpl implements SecurityFrameworkService {
    private final PermissionCommonApi permissionApi;

    @Override
    public boolean hasPermission(String permission) {
        return hasAnyPermissions(permission);
    }

    @Override
    public boolean hasAnyPermissions(String... permissions) {
        // Special case: cross-tenant access
        if(skipPermissionCheck()) {
            return true;
        }

        // Permission check
        Long userId = getLoginUserId();
        if(userId == null) {
            return false;
        }
        return permissionApi.hasAnyPermissions(userId, permissions);
    }

    @Override
    public boolean hasRole(String role) {
        return hasAnyRoles(role);
    }

    @Override
    public boolean hasAnyRoles(String... roles) {
        // Special case: cross-tenant access
        if(skipPermissionCheck()) {
            return true;
        }

        // Role check
        Long userId = getLoginUserId();
        if(userId == null) {
            return false;
        }
        return permissionApi.hasAnyRoles(userId, roles);
    }

    @Override
    public boolean hasScope(String scope) {
        return hasAnyScopes(scope);
    }

    @Override
    public boolean hasAnyScopes(String... scope) {
        // Special case: cross-tenant access
        if(skipPermissionCheck()) {
            return true;
        }

        // Permission validation
        LoginUser user = SecurityFrameworkUtils.getLoginUser();
        if(user == null) {
            return false;
        }
        return CollUtil.containsAny(user.getScopes(), Arrays.asList(scope));
    }
}
