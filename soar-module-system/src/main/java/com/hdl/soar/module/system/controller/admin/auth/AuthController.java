package com.hdl.soar.module.system.controller.admin.auth;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.security.config.SecurityProperties;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthLoginReqDTO;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthLoginRespDTO;
import com.hdl.soar.module.system.enums.logger.LoginLogTypeEnum;
import com.hdl.soar.module.system.service.auth.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Authentication")
@Slf4j
@Validated
@RestController
@RequestMapping("/system/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    SecurityProperties securityProperties;

    AdminAuthService authService;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "Login with username and password")
    public CommonResult<AuthLoginRespDTO> login(@Valid @RequestBody AuthLoginReqDTO reqDTO) {
        return success(authService.login(reqDTO));
    }

    @PostMapping("/logout")
    @PermitAll
    @Operation(summary = "Logout from the system")
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        }
        return success(true);
    }

}
