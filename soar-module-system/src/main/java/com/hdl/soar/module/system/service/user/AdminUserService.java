package com.hdl.soar.module.system.service.user;

import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;

/**
 * Backend user service interface
 */
public interface AdminUserService {

    /**
     * Retrieve user by username
     *
     * @param username the username
     * @return user object information
     */
    AdminUserPO getUserByUsername(String username);

    /**
     * Query user by user ID
     *
     * @param id User ID
     * @return User object information
     */
    AdminUserPO getUser(Long id);

    /**
     * Check if the password matches
     *
     * @param rawPassword unencrypted password
     * @param encodedPassword encrypted password
     * @return whether the passwords match
     */
    boolean isPasswordMatch(String rawPassword, String encodedPassword);

}
