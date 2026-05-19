package com.hdl.soar.framework.security.core.handler;

import org.springframework.security.web.access.ExceptionTranslationFilter;
import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;

/**
 * Access a URL resource that requires authentication, but the user is not authenticated (logged in) at this time.
 * In this case, return the {@link GlobalErrorCodeConstants#UNAUTHORIZED} error code, so that the frontend can redirect to the login page.
 *
 * Note: Spring Security calls this class via the
 * {@link ExceptionTranslationFilter#sendStartAuthentication(HttpServletRequest, HttpServletResponse, FilterChain, AuthenticationException)}
 * method.
 *
 */
@Slf4j
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException, ServletException {
        log.debug("[commence][When accessing URL ({}), the user is not logged in]", request.getRequestURI(), e);
        // Return 401
        ServletUtils.writeJSON(response, CommonResult.error(UNAUTHORIZED));
    }
}
