package com.hdl.soar.module.system.api.permission;

import com.hdl.soar.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * Permission API Implementation Class
 *
 * @author YuDao Source Code
 */
@Service
public class PermissionApiImpl implements PermissionApi {
    @Override
    public Set<Long> getUserRoleIdListByRoleIds(Collection<Long> roleIds) {
        return Set.of();
    }

    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        return false;
    }

    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        return false;
    }

    @Override
    public DeptDataPermissionRespDTO getDeptDataPermission(Long userId) {
        return null;
    }
}
