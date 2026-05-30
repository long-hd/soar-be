package com.hdl.soar.module.system.service.user;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.system.controller.admin.user.dto.profile.UserProfileUpdatePasswordReqDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.profile.UserProfileUpdateReqDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserImportExcelDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserImportRespDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserPageReqDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Backend user service interface
 */
public interface AdminUserService {

    /**
     * Create a user
     *
     * @param createReqDTO user information
     * @return user ID
     */
    Long createUser(UserSaveReqDTO createReqDTO);

    /**
     * Update user information
     *
     * @param updateReqDTO user information
     */
    void updateUser(UserSaveReqDTO updateReqDTO);

    /**
     * Delete a user
     *
     * @param id user ID
     */
    void deleteUser(Long id);

    /**
     * Delete users in batch
     *
     * @param ids list of user IDs
     */
    void deleteUserList(List<Long> ids);

    /**
     * Admin resets password for a user (no old password required).
     *
     * @param id user ID
     * @param password password
     */
    void updateUserPassword(Long id, String password);

    /**
     * Update user status (enable/disable).
     *
     * @param id user ID
     * @param status status
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * Get user paginated list
     *
     * @param pageReqDTO pagination conditions
     * @return paginated list
     */
    PageResult<AdminUserPO> getUserPage(UserPageReqDTO pageReqDTO);

    /**
     * Import users in batch
     *
     * @param importUsers list of users to import
     * @param updateSupport whether update is supported
     * @return import result
     */
    UserImportRespDTO importUserList(List<UserImportExcelDTO> importUsers, boolean updateSupport);

    /**
     * Get user list base on status
     *
     * @param status user status
     * @return the user list
     */
    List<AdminUserPO> getUserListByStatus(CommonStatusEnum status);

    /**
     * Update the user's last login information
     *
     * @param id user ID
     * @param loginIp login IP address
     */
    void updateUserLoginIp(Long id, String loginIp);

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


    /**
     * User self-updates their own profile (nickname, email, mobile, sex, avatar).
     *
     * @param userId user ID (from token)
     * @param reqDTO profile update data
     */
    void updateUserProfile(Long userId, UserProfileUpdateReqDTO reqDTO);

    /**
     * User self-changes their own password (requires old password verification).
     *
     * @param userId user ID (from token)
     * @param reqDTO old + new password
     */
    void updateUserProfilePassword(Long userId, UserProfileUpdatePasswordReqDTO reqDTO);

}
