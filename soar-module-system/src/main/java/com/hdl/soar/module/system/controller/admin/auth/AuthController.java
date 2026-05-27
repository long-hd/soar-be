package com.hdl.soar.module.system.controller.admin.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.security.config.SecurityProperties;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import com.hdl.soar.module.system.controller.admin.auth.dto.*;
import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.enums.logger.LoginLogTypeEnum;
import com.hdl.soar.module.system.mapper.auth.AuthMapper;
import com.hdl.soar.module.system.service.auth.AdminAuthService;
import com.hdl.soar.module.system.service.permission.MenuService;
import com.hdl.soar.module.system.service.permission.PermissionService;
import com.hdl.soar.module.system.service.permission.RoleService;
import com.hdl.soar.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;
import static com.hdl.soar.framework.common.util.collection.CollectionUtils.convertSet;

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
    AdminUserService userService;
    RoleService roleService;
    MenuService menuService;
    PermissionService permissionService;

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

    @PostMapping("/refresh-token")
    @PermitAll
    @Operation(summary = "Refresh token")
    @Parameter(name = "refreshToken", description = "Refresh token", required = true)
    public CommonResult<AuthLoginRespDTO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(authService.refreshToken(refreshToken));
    }

    @GetMapping("/get-permission-info")
    @Operation(summary = "Get permission information of the logged-in user")
    // @DataPermission(enable = false) // Ignore data permissions to avoid filtering issues that may prevent querying the user. Similar to: https://t.zsxq.com/LHnrp
    public CommonResult<AuthPermissionInfoRespDTO> getPermissionInfo() {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 1.1 Get user information
        AdminUserPO user = userService.getUser(loginUserId);
        if (user == null) {
            return success(null);
        }

        // 1.2 Get role list
        Set<Long> roleIds = roleService.getRoleIdsByUserId(loginUserId);
        if (CollUtil.isEmpty(roleIds)) {
            return success(AuthMapper.INSTANCE.convert(user, Collections.emptyList(), Collections.emptyList()));
        }

        List<RolePO> roles = roleService.getRolesByIdIn(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus())); // Remove disabled roles

        // 1.3 Get menu list
        Set<Long> menuIds = permissionService.getMenuIdsByRoleIds(convertSet(roles, RolePO::getId));
        List<MenuPO> menuList = menuService.getMenuList(menuIds);
        menuList = menuService.filterDisableMenus(menuList);

        // 2. Build and return the result
        return success(AuthMapper.INSTANCE.convert(user, roles, menuList));
    }

}
