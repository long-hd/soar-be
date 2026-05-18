package com.hdl.soar.framework.web.core.filter;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.framework.web.core.util.WebFrameworkUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants.DEMO_DENY;

/**
 * Demo filter that prevents users from performing write operations,
 * to avoid affecting test data.
 */
public class DemoFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        return !StrUtil.equalsAnyIgnoreCase(method, "POST", "PUT", "DELETE")  // Do not filter for write operations
                || WebFrameworkUtils.getLoginUserId(request) == null; // Do not filter for non-logged-in users
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Directly return the DEMO_DENY result, i.e., do not continue processing the request
        ServletUtils.writeJSON(response, CommonResult.error(DEMO_DENY));
    }

}
