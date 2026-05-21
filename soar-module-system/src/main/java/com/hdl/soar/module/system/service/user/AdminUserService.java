package com.hdl.soar.module.system.service.user;

import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;

/**
 * Backend user service interface
 */
public interface AdminUserService {

    /**
     * Query user by user ID
     *
     * @param id User ID
     * @return User object information
     */
    AdminUserPO getUser(Long id);

}
