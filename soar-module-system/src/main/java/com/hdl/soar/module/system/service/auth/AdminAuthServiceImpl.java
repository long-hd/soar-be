package com.hdl.soar.module.system.service.auth;

import cn.hutool.core.util.ObjectUtil;
import com.anji.captcha.model.vo.CaptchaVO;
import com.google.common.annotations.VisibleForTesting;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.util.monitor.TracerUtils;
import com.hdl.soar.framework.common.util.object.BeanUtils;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.framework.common.util.validation.ValidationUtils;
import com.hdl.soar.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.hdl.soar.module.system.api.social.dto.SocialUserBindReqDTO;
import com.hdl.soar.module.system.controller.admin.auth.dto.*;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.enums.logger.LoginLogTypeEnum;
import com.hdl.soar.module.system.enums.logger.LoginResultEnum;
import com.hdl.soar.module.system.enums.oauth2.OAuth2ClientConstants;
import com.hdl.soar.module.system.service.logger.LoginLogService;
import com.hdl.soar.module.system.service.oauth2.OAuth2TokenService;
import com.hdl.soar.module.system.service.social.SocialUserService;
import com.hdl.soar.module.system.service.user.AdminUserService;
import com.anji.captcha.model.common.ResponseModel;
import jakarta.validation.Validator;
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
    SocialUserService socialUserService;
    OAuth2TokenService oauth2TokenService;
//    CaptchaService captchaService;
    Validator validator;

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
    // @DataPermission(enable = false)
    public AuthLoginRespDTO login(AuthLoginReqDTO reqDTO) {
        // Validate captcha
        validateCaptcha(reqDTO);

        // Use username and password to log in
        AdminUserPO user = authenticate(reqDTO.getUsername(), reqDTO.getPassword());

        // If socialType is not null, it means a social user needs to be bound
        if (reqDTO.getSocialType() != null) {
            socialUserService.bindSocialUser(new SocialUserBindReqDTO(
                    user.getId(),
                    getUserType().getValue(),
                    reqDTO.getSocialType(),
                    reqDTO.getSocialCode(),
                    reqDTO.getSocialState()));
        }

        // Create token and record login log
        return createTokenAfterLoginSuccess(
                user.getId(),
                reqDTO.getUsername(),
                LoginLogTypeEnum.LOGIN_USERNAME);
    }

    @Override
    public void logout(String token, Integer logType) {
        // Remove access token
        OAuth2AccessTokenPO accessTokenPO = oauth2TokenService.removeAccessToken(token);
        if (accessTokenPO == null) {
            return;
        }

        // If removal is successful, record logout log
        createLogoutLog(accessTokenPO.getUserId(), accessTokenPO.getUserType(), logType);
    }

    private void createLogoutLog(Long userId, Integer userType, Integer logType) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logType);
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(userType);
        if (ObjectUtil.equal(getUserType().getValue(), userType)) {
            reqDTO.setUsername(getUsername(userId));
        } else {
            reqDTO.setUsername("Member");
        }
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogService.createLoginLog(reqDTO);
    }

    private String getUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        AdminUserPO user = userService.getUser(userId);
        return user != null ? user.getUsername() : null;
    }

    @Override
    public AuthLoginRespDTO refreshToken(String refreshToken) {
        OAuth2AccessTokenPO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, OAuth2ClientConstants.CLIENT_ID_DEFAULT);
        return BeanUtils.toBean(accessTokenDO, AuthLoginRespDTO.class);
    }

    @Override
    public AuthLoginRespDTO register(AuthRegisterReqDTO createReqDTO) {
        return null;
    }

    @Override
    public void resetPassword(AuthResetPasswordReqDTO reqDTO) {

    }

    @VisibleForTesting
    void validateCaptcha(AuthLoginReqDTO reqDTO) {
        ResponseModel response = doValidateCaptcha(reqDTO);

        // Validate captcha
        if (!response.isSuccess()) {
            // Create login failure log (incorrect captcha)
            createLoginLog(
                    null,
                    reqDTO.getUsername(),
                    LoginLogTypeEnum.LOGIN_USERNAME,
                    LoginResultEnum.CAPTCHA_CODE_ERROR);

            throw exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR, response.getRepMsg());
        }
    }

    private ResponseModel doValidateCaptcha(CaptchaVerificationReqDTO reqDTO) {
        // If captcha is disabled, skip validation
        if (!captchaEnable) {
            return ResponseModel.success();
        }
        ValidationUtils.validate(validator, reqDTO, CaptchaVerificationReqDTO.CodeEnableGroup.class);
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(reqDTO.getCaptchaVerification());
//        return captchaService.verification(captchaVO);
        return null;
    }

    private AuthLoginRespDTO createTokenAfterLoginSuccess(Long userId, String username, LoginLogTypeEnum logType) {
        // Insert login log
        createLoginLog(userId, username, logType, LoginResultEnum.SUCCESS);

        // Create access token
        OAuth2AccessTokenPO accessTokenDO = oauth2TokenService.createAccessToken(
                userId,
                getUserType().getValue(),
                OAuth2ClientConstants.CLIENT_ID_DEFAULT,
                null
        );

        // Build response result
        return BeanUtils.toBean(accessTokenDO, AuthLoginRespDTO.class);
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
            userService.updateUserLoginIp(userId, ServletUtils.getClientIP());
        }
    }

    private UserTypeEnum getUserType() {
        return UserTypeEnum.ADMIN;
    }

}
