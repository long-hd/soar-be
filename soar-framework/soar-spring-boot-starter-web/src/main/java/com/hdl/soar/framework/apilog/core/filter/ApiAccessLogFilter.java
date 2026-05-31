package com.hdl.soar.framework.apilog.core.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.hdl.soar.framework.apilog.core.annotation.ApiAccessLog;
import com.hdl.soar.framework.common.enums.OperateTypeEnum;
import com.hdl.soar.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.common.util.monitor.TracerUtils;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.framework.web.config.WebProperties;
import com.hdl.soar.framework.web.core.filter.ApiRequestFilter;
import com.hdl.soar.framework.web.core.util.WebFrameworkUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

import static cn.hutool.core.exceptions.ExceptionUtil.getRootCauseMessage;
import static com.hdl.soar.framework.apilog.core.interceptor.ApiAccessLogInterceptor.ATTRIBUTE_HANDLER_METHOD;
import static com.hdl.soar.framework.common.util.json.JsonUtils.toJsonString;

/**
 * API Access Log Filter.
 * <p>
 * Wraps every API request, captures timing/user/request/response metadata,
 * then delegates persistence to {@link ApiAccessLogCommonApi}.
 */
@Slf4j
public class ApiAccessLogFilter extends ApiRequestFilter {

    private static final String[] SANITIZE_KEYS = {"password", "token", "accessToken", "refreshToken"};

    private final String applicationName;
    private final ApiAccessLogCommonApi apiAccessLogApi;

    public ApiAccessLogFilter(WebProperties webProperties, String applicationName,
                              ApiAccessLogCommonApi apiAccessLogApi) {
        super(webProperties);
        this.applicationName = applicationName;
        this.apiAccessLogApi = apiAccessLogApi;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Capture begin time and request data before filter chain (before XssFilter etc.)
        Instant beginTime = Instant.now();
        Map<String, String> queryString = ServletUtils.getParamMap(request);
        String requestBody = ServletUtils.isJsonRequest(request) ? ServletUtils.getBody(request) : null;

        try {
            filterChain.doFilter(request, response);
            createApiAccessLog(request, beginTime, queryString, requestBody, null);
        } catch (Exception ex) {
            createApiAccessLog(request, beginTime, queryString, requestBody, ex);
            throw ex;
        }
    }

    private void createApiAccessLog(HttpServletRequest request, Instant beginTime,
                                    Map<String, String> queryString, String requestBody,
                                    Exception ex) {
        ApiAccessLogCreateReqDTO accessLog = new ApiAccessLogCreateReqDTO();
        try {
            boolean enable = buildApiAccessLog(accessLog, request, beginTime, queryString, requestBody, ex);
            if (!enable) {
                return;
            }
            apiAccessLogApi.createApiAccessLogAsync(accessLog);
        } catch (Throwable th) {
            log.error("[createApiAccessLog][url({}) log({}) exception]",
                    request.getRequestURI(), toJsonString(accessLog), th);
        }
    }

    private boolean buildApiAccessLog(ApiAccessLogCreateReqDTO accessLog, HttpServletRequest request,
                                      Instant beginTime, Map<String, String> queryString,
                                      String requestBody, Exception ex) {
        // 1. Check @ApiAccessLog.enable
        HandlerMethod handlerMethod = (HandlerMethod) request.getAttribute(ATTRIBUTE_HANDLER_METHOD);
        ApiAccessLog annotation = null;
        if (handlerMethod != null) {
            annotation = handlerMethod.getMethodAnnotation(ApiAccessLog.class);
            if (annotation != null && BooleanUtil.isFalse(annotation.enable())) {
                return false;
            }
        }

        // 2. User info
        accessLog.setUserId(WebFrameworkUtils.getLoginUserId(request));
        accessLog.setUserType(WebFrameworkUtils.getLoginUserType(request));

        // 3. Result code/msg
        CommonResult<?> result = WebFrameworkUtils.getCommonResult(request);
        if (result != null) {
            accessLog.setResultCode(result.getCode());
            accessLog.setResultMsg(result.getMsg());
        } else if (ex != null) {
            accessLog.setResultCode(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode());
            accessLog.setResultMsg(getRootCauseMessage(ex));
        } else {
            accessLog.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            accessLog.setResultMsg("");
        }

        // 4. Request metadata
        accessLog.setTraceId(TracerUtils.getTraceId());
        accessLog.setApplicationName(applicationName);
        accessLog.setRequestUrl(request.getRequestURI());
        accessLog.setRequestMethod(request.getMethod());
        accessLog.setUserAgent(ServletUtils.getUserAgent(request));
        accessLog.setUserIp(ServletUtils.getClientIP(request));

        // 5. Request params (sanitized)
        String[] sanitizeKeys = annotation != null ? annotation.sanitizeKeys() : null;
        boolean requestEnable = annotation == null || annotation.requestEnable();
        if (requestEnable) {
            Map<String, Object> requestParams = MapUtil.<String, Object>builder()
                    .put("query", sanitizeMap(queryString, sanitizeKeys))
                    .put("body", sanitizeJson(requestBody, sanitizeKeys))
                    .build();
            accessLog.setRequestParams(toJsonString(requestParams));
        }

        // 6. Response body (opt-in)
        boolean responseEnable = annotation != null && annotation.responseEnable();
        if (responseEnable) {
            accessLog.setResponseBody(sanitizeJson(toJsonString(result), sanitizeKeys));
        }

        // 7. Timing
        Instant endTime = Instant.now();
        accessLog.setBeginTime(beginTime);
        accessLog.setEndTime(endTime);
        accessLog.setDuration((int) Duration.between(beginTime, endTime).toMillis());

        // 8. Operation module/name/type from annotation or Swagger
        if (handlerMethod != null) {
            Tag tagAnnotation = handlerMethod.getBeanType().getAnnotation(Tag.class);
            Operation operationAnnotation = handlerMethod.getMethodAnnotation(Operation.class);

            String operateModule = annotation != null && StrUtil.isNotBlank(annotation.operateModule())
                    ? annotation.operateModule()
                    : tagAnnotation != null ? StrUtil.nullToDefault(tagAnnotation.name(), tagAnnotation.description()) : null;

            String operateName = annotation != null && StrUtil.isNotBlank(annotation.operateName())
                    ? annotation.operateName()
                    : operationAnnotation != null ? operationAnnotation.summary() : null;

            OperateTypeEnum operateType = annotation != null && annotation.operateType().length > 0
                    ? annotation.operateType()[0]
                    : parseOperateType(request);

            accessLog.setOperateModule(operateModule);
            accessLog.setOperateName(operateName);
            accessLog.setOperateType(operateType.getType());
        }
        return true;
    }

    // ========== Operate type inference ==========

    private static OperateTypeEnum parseOperateType(HttpServletRequest request) {
        RequestMethod requestMethod = RequestMethod.resolve(request.getMethod());
        if (requestMethod == null) {
            return OperateTypeEnum.OTHER;
        }
        return switch (requestMethod) {
            case GET -> OperateTypeEnum.GET;
            case POST -> OperateTypeEnum.CREATE;
            case PUT -> OperateTypeEnum.UPDATE;
            case DELETE -> OperateTypeEnum.DELETE;
            default -> OperateTypeEnum.OTHER;
        };
    }

    // ========== Sanitize logic: remove sensitive keys from request/response ==========

    private static String sanitizeMap(Map<String, ?> map, String[] sanitizeKeys) {
        if (CollUtil.isEmpty(map)) {
            return null;
        }
        if (sanitizeKeys != null) {
            MapUtil.removeAny(map, sanitizeKeys);
        }
        MapUtil.removeAny(map, SANITIZE_KEYS);
        return toJsonString(map);
    }

    private static String sanitizeJson(String jsonString, String[] sanitizeKeys) {
        if (StrUtil.isEmpty(jsonString)) {
            return null;
        }
        try {
            JsonNode rootNode = JsonUtils.parseTree(jsonString);
            sanitizeJsonNode(rootNode, sanitizeKeys);
            return toJsonString(rootNode);
        } catch (Exception e) {
            log.error("[sanitizeJson][sanitize({}) exception]", jsonString, e);
            return jsonString;
        }
    }

    private static void sanitizeJsonNode(JsonNode node, String[] sanitizeKeys) {
        if (node == null) {
            return;
        }
        // Case 1: array — recurse each element
        if (node.isArray()) {
            for (JsonNode child : node) {
                sanitizeJsonNode(child, sanitizeKeys);
            }
            return;
        }
        // Case 2: not object — leaf value, nothing to sanitize
        if (!node.isObject()) {
            return;
        }
        // Case 3: object — remove sensitive keys, recurse remaining
        Iterator<Map.Entry<String, JsonNode>> it = node.properties().iterator();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            if (ArrayUtil.contains(sanitizeKeys, entry.getKey())
                    || ArrayUtil.contains(SANITIZE_KEYS, entry.getKey())) {
                it.remove();
                continue;
            }
            sanitizeJsonNode(entry.getValue(), sanitizeKeys);
        }
    }


}
