package com.hdl.soar.module.system.service.auth;

import com.hdl.soar.module.system.controller.admin.auth.dto.AuthLoginReqDTO;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthLoginRespDTO;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthRegisterReqDTO;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthResetPasswordReqDTO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import jakarta.validation.Valid;

/**
 * Admin Backend Authentication Service Interface
 * <br>
 * Provides capabilities for user login and logout
 */
public interface AdminAuthService {

    /**
     * Verify username + password. If successful, return the user
     *
     * @param username username
     * @param password password
     * @return user
     */
    AdminUserPO authenticate(String username, String password);

    /**
     * Username-password login
     *
     * @param reqDTO login request
     * @return login result
     */
    AuthLoginRespDTO login(@Valid AuthLoginReqDTO reqDTO);

    /**
     * Logout based on token
     *
     * @param token token
     * @param logType logout type
     */
    void logout(String token, Integer logType);

    /**
     * Refresh access token
     *
     * @param refreshToken refresh token
     * @return login result
     */
    AuthLoginRespDTO refreshToken(String refreshToken);

    /**
     * User registration
     *
     * @param createReqDTO registration request
     * @return registration result
     */
    AuthLoginRespDTO register(AuthRegisterReqDTO createReqDTO);

    /**
     * Reset password
     *
     * @param reqDTO verification code info
     */
    void resetPassword(AuthResetPasswordReqDTO reqDTO);

}
