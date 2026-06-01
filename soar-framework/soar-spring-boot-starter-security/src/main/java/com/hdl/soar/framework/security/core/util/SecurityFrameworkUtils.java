package com.hdl.soar.framework.security.core.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.web.core.util.WebFrameworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;

import java.util.Collections;

/**
 * Security service utility class
 */
public class SecurityFrameworkUtils {
    /**
     * Prefix for the value of the authentication header (HTTP HEADER)
     */
    public static final String AUTHORIZATION_BEARER = "Bearer";

    private SecurityFrameworkUtils() {}

    /**
     * Extract the authentication token from the request.
     *
     * @param request the HTTP request
     * @param headerName the name of the header that contains the authentication token
     * @param parameterName the name of the request parameter that may contain the authentication token
     * @return the authentication token
     */
    public static String obtainAuthorization(HttpServletRequest request,
                                             String headerName, String parameterName) {
        // 1. Get the token. Priority: Header > Parameter
        String token = request.getHeader(headerName);
        if (StrUtil.isEmpty(token)) {
            token = request.getParameter(parameterName);
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        // 2. Remove the "Bearer" prefix from the token
        int index = token.indexOf(AUTHORIZATION_BEARER + " ");
        return index >= 0 ? token.substring(index + 7).trim() : token;
    }

    /**
     * Get the current authentication information.
     *
     * @return the authentication information
     */
    public static Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        return context.getAuthentication();
    }

    /**
     * Get the current user.
     *
     * @return the current user
     */
    @Nullable
    public static LoginUser getLoginUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getPrincipal() instanceof LoginUser ? (LoginUser) authentication.getPrincipal() : null;
    }

    /**
     * Get the ID of the current user from the context.
     *
     * @return the user ID
     */
    @Nullable
    public static Long getLoginUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getId() : null;
    }

    /**
     * Get the nickname of the current user from the context.
     *
     * @return the nickname
     */
    @Nullable
    public static String getLoginUserNickname() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? MapUtil.getStr(loginUser.getInfo(), LoginUser.INFO_KEY_NICKNAME) : null;
    }

    /**
     * Get the department ID of the current user from the context.
     *
     * @return the department ID
     */
    @Nullable
    public static Long getLoginUserDeptId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? MapUtil.getLong(loginUser.getInfo(), LoginUser.INFO_KEY_DEPT_ID) : null;
    }

    /**
     * Set the current user.
     *
     * @param loginUser the logged-in user
     * @param request the HTTP request
     */
    public static void setLoginUser(LoginUser loginUser, HttpServletRequest request) {
        // Create Authentication and set it into the context
        Authentication authentication = buildAuthentication(loginUser, request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Additionally set it into the request so that ApiAccessLogFilter can obtain the user ID.
        // The reason is that Spring Security filters are executed after ApiAccessLogFilter,
        // so when access logs are recorded, the security context does not yet contain user information such as the user ID.
        if(request != null) {
            WebFrameworkUtils.setLoginUserId(request, loginUser.getId());
            WebFrameworkUtils.setLoginUserType(request, loginUser.getUserType());
        }
    }

    private static Authentication buildAuthentication(LoginUser loginUser, HttpServletRequest request) {
        // Create a UsernamePasswordAuthenticationToken object
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginUser, null, Collections.emptyList());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authenticationToken;
    }

    /**
     * Whether to conditionally skip permission checks, including data permissions and functional permissions.
     *
     * @return whether to skip
     */
    public static boolean skipPermissionCheck() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        if (loginUser.getVisitTenantId() == null) {
            return false;
        }
        // Key point: When accessing across tenants, permission checks cannot be performed.
        return ObjUtil.notEqual(loginUser.getVisitTenantId(), loginUser.getTenantId());
    }

}
