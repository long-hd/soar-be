package com.hdl.soar.module.system.api.permission;

import com.hdl.soar.framework.common.biz.system.permission.PermissionCommonApi;

import java.util.Collection;
import java.util.Set;

/**
 * Permission API Interface
 */
public interface PermissionApi extends PermissionCommonApi {

    /**
     * Get the collection of user IDs that have multiple roles
     *
     * @param roleIds collection of role IDs
     * @return collection of user IDs
     */
    Set<Long> getUserIdsByRoleIds(Collection<Long> roleIds);

}
