package com.hdl.soar.framework.apilog.core.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * API Access Log Interceptor.
 * <p>
 * Two responsibilities:
 * 1. Store {@link HandlerMethod} as request attribute for {@code ApiAccessLogFilter} to read.
 * 2. Print request/response logs to console in non-prod environments.
 */
@Slf4j
public class ApiAccessLogInterceptor implements HandlerInterceptor {

    public static final String ATTRIBUTE_HANDLER_METHOD = "HANDLER_METHOD";

    private final boolean isProd;

    public ApiAccessLogInterceptor(boolean isProd) {
        this.isProd = isProd;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // 1. Store HandlerMethod for ApiAccessLogFilter
        if (handler instanceof HandlerMethod handlerMethod) {
            request.setAttribute(ATTRIBUTE_HANDLER_METHOD, handlerMethod);
        }

        // 2. Console log in non-prod
        if (!isProd) {
            Map<String, String> queryString = ServletUtils.getParamMap(request);
            String requestBody = ServletUtils.isJsonRequest(request) ? ServletUtils.getBody(request) : null;
            if (CollUtil.isEmpty(queryString) && StrUtil.isEmpty(requestBody)) {
                log.info("[preHandle][Request URL({}) no params]", request.getRequestURI());
            } else {
                log.info("[preHandle][Request URL({}) params({})]", request.getRequestURI(),
                        StrUtil.blankToDefault(requestBody, queryString.toString()));
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // Console log in non-prod only
        if (!isProd) {
            log.info("[afterCompletion][Completed URL({})]", request.getRequestURI());
        }
    }

}
