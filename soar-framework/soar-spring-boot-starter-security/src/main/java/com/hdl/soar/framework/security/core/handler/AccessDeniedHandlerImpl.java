package com.hdl.soar.framework.security.core.handler;

import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import static com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;

/**
 * Access a URL resource that requires authentication, where the user is already authenticated (logged in)
 * but does not have sufficient permissions. In this case, return the {@link GlobalErrorCodeConstants#FORBIDDEN}
 * error code.
 *
 * Note: Spring Security calls this class via the
 * {@link ExceptionTranslationFilter#handleAccessDeniedException(HttpServletRequest, HttpServletResponse, FilterChain, AccessDeniedException)}
 * method.
 */
@Slf4j
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException, ServletException {
        // Log at WARN level because we periodically review warnings to detect potential malicious activity
        log.warn("[handle][When accessing URL ({}), user ({}) has insufficient permissions]",
                request.getRequestURI(),
                SecurityFrameworkUtils.getLoginUserId(),
                e);

        // Return 403
        ServletUtils.writeJSON(response, CommonResult.error(FORBIDDEN));
    }
}
