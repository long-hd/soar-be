package com.hdl.soar.module.system.service.auth;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.util.monitor.TracerUtils;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthLoginReqDTO;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthLoginRespDTO;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthRegisterReqDTO;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthResetPasswordReqDTO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.enums.logger.LoginLogTypeEnum;
import com.hdl.soar.module.system.enums.logger.LoginResultEnum;
import com.hdl.soar.module.system.service.logger.LoginLogService;
import com.hdl.soar.module.system.service.user.AdminUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;

/**
 * Implementation class for Auth Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAuthServiceImpl implements AdminAuthService {

    /**
     * Captcha toggle, defaults to true
     */
    @Value("${soar.captcha.enable:true}")
    @NonFinal @Setter // For unit testing: enable or disable captcha
    private Boolean captchaEnable;

    AdminUserService userService;
    LoginLogService loginLogService;

    @Override
    public AdminUserPO authenticate(String username, String password) {
        final LoginLogTypeEnum logTypeEnum = LoginLogTypeEnum.LOGIN_USERNAME;
        // Check if the account exists
        AdminUserPO user = userService.getUserByUsername(username);
        if (user == null) {
            createLoginLog(null, username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        if (!userService.isPasswordMatch(password, user.getPassword())) {
            createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // Check if the user is disabled
        if (CommonStatusEnum.isDisable(user.getStatus())) {
            createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }
        return user;
    }

    @Override
    public AuthLoginRespDTO login(AuthLoginReqDTO reqVO) {
        return null;
    }

    @Override
    public void logout(String token, Integer logType) {

    }

    @Override
    public AuthLoginRespDTO refreshToken(String refreshToken) {
        return null;
    }

    @Override
    public AuthLoginRespDTO register(AuthRegisterReqDTO createReqVO) {
        return null;
    }

    @Override
    public void resetPassword(AuthResetPasswordReqDTO reqVO) {

    }

    private void createLoginLog(Long userId, String username,
                                LoginLogTypeEnum logTypeEnum, LoginResultEnum loginResult) {
        // Insert login log
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logTypeEnum.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(getUserType().getValue());
        reqDTO.setUsername(username);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(loginResult.getResult());
        loginLogService.createLoginLog(reqDTO);
        // Update last login time
        if (userId != null && Objects.equals(LoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
            userService.updateUserLogin(userId, ServletUtils.getClientIP());
        }
    }

    private UserTypeEnum getUserType() {
        return UserTypeEnum.ADMIN;
    }

}
