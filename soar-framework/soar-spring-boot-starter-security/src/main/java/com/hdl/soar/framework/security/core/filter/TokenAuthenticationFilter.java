package com.hdl.soar.framework.security.core.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.hdl.soar.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import com.hdl.soar.framework.common.exception.ServiceException;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.framework.security.config.SecurityProperties;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import com.hdl.soar.framework.web.core.handler.GlobalExceptionHandler;
import com.hdl.soar.framework.web.core.util.WebFrameworkUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Token filter that validates the validity of the token.
 * After successful validation, it retrieves the {@link LoginUser} information
 * and adds it to the Spring Security context.
 */
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final OAuth2TokenCommonApi oauth2TokenApi;


    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (StrUtil.isNotEmpty(token)) {
            Integer userType = WebFrameworkUtils.getLoginUserType(request);
            try {
                // 1.1 Build the login user based on the token
                LoginUser loginUser = buildLoginUserByToken(token, userType);

                // 1.2 Mock login functionality for easier local development and debugging
                if (loginUser == null) {
                    loginUser = mockLoginUser(request, token, userType);
                }

                // 2. Set the current user
                if (loginUser != null) {
                    SecurityFrameworkUtils.setLoginUser(loginUser, request);
                }
            } catch (Throwable ex) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }

        // Continue the filter chain
        chain.doFilter(request, response);
    }

    private LoginUser buildLoginUserByToken(String token, Integer userType) {
        try{
            OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token);

            // User type mismatch, no permission granted
            // Note: Only /admin-api/* and /app-api/* endpoints include userType and require user type validation.
            // For example, WebSocket connections such as /ws/* do not require user type verification.
            if (userType != null
                    && ObjectUtil.notEqual(accessToken.getUserType(), userType)) {
                throw new AccessDeniedException("Incorrect user type");
            }

            // Build the login user
            return LoginUser.builder()
                    .id(accessToken.getUserId())
                    .userType(accessToken.getUserType())
                    .info(accessToken.getUserInfo()) // additional user information
                    .tenantId(accessToken.getTenantId())
                    .scopes(accessToken.getScopes())
                    .expiresTime(accessToken.getExpiresTime())
                    .build();
        } catch (ServiceException serviceException) {
            // If token validation fails, return null directly because some APIs do not require authentication
            return null;
        }
    }

    /**
     * Mock a login user for daily development and debugging.
     *
     * <p>Note: This feature must be disabled in production environments!!!
     *
     * @param request the HTTP request
     * @param token the mock token, format: {@link SecurityProperties#getMockSecret()} + user ID
     * @param userType the user type
     * @return the mocked LoginUser
     */
    private LoginUser mockLoginUser(HttpServletRequest request, String token, Integer userType) {
        if (!securityProperties.getMockEnable()) {
            return null;
        }

        // Must start with mockSecret
        if (!token.startsWith(securityProperties.getMockSecret())) {
            return null;
        }

        // Build mock user
        Long userId = Long.valueOf(token.substring(securityProperties.getMockSecret().length()));
        return LoginUser.builder()
                .id(userId)
                .userType(userType)
                .tenantId(WebFrameworkUtils.getTenantId(request))
                .build();
    }

}
