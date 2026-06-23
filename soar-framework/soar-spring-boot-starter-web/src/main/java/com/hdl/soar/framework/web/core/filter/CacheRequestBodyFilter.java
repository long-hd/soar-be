package com.hdl.soar.framework.web.core.filter;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Request body caching filter that enables repeated reading of the request body.
 */
public class CacheRequestBodyFilter extends OncePerRequestFilter {

    /**
     * URIs that need to be excluded.
     * <p>
     * 1. Exclude Spring Boot Admin related requests to avoid exceptions
     *    caused by client connection interruptions.
     *    For example: <a href="https://github.com/YunaiV/ruoyi-vue-pro/issues/795">Issue #795</a>
     */
    private static final String[] IGNORE_URIS = {"/admin/", "/actuator/"};

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new CacheRequestBodyWrapper(request), response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 1. Check whether the URL is excluded
        String requestURI = request.getRequestURI();
        if(StrUtil.startWithAny(requestURI, IGNORE_URIS)) {
            return true;
        }

        // 2. Only process JSON request content
        return !ServletUtils.isJsonRequest(request);
    }
}
