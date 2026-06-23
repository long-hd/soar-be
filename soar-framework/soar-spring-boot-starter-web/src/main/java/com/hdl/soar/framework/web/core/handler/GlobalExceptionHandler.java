package com.hdl.soar.framework.web.core.handler;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.hdl.soar.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import com.hdl.soar.framework.common.exception.ServiceException;
import com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.util.collection.SetUtils;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.common.util.monitor.TracerUtils;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.framework.web.core.util.WebFrameworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants.*;

/**
 * Global exception handler, translates Exception into CommonResult + corresponding error code
 */
@RestControllerAdvice
@Slf4j
@AllArgsConstructor
public class GlobalExceptionHandler {
    /**
     * Ignored ServiceException error messages to avoid excessive logging
     */
    public static final Set<String> IGNORE_ERROR_MESSAGES = SetUtils.asSet("Invalid refresh token");

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String applicationName;

    private final ApiErrorLogCommonApi apiErrorLogApi;

    /**
     * Handles all exceptions, mainly for use by Filters.
     * Since Filters do not go through the Spring MVC processing flow, but we still need a fallback mechanism for exception handling,
     * this provides a full exception-handling process to ensure consistent logic.
     *
     * @param request the request
     * @param ex the exception
     * @return a generic response
     */
    public CommonResult<?> allExceptionHandler(HttpServletRequest request, Throwable ex) {
        if (ex instanceof MissingServletRequestParameterException) {
            return missingServletRequestParameterExceptionHandler((MissingServletRequestParameterException) ex);
        }
        if (ex instanceof MethodArgumentTypeMismatchException) {
            return methodArgumentTypeMismatchExceptionHandler((MethodArgumentTypeMismatchException) ex);
        }
        if (ex instanceof MethodArgumentNotValidException) {
            return methodArgumentNotValidExceptionExceptionHandler((MethodArgumentNotValidException) ex);
        }
        if (ex instanceof BindException) {
            return bindExceptionHandler((BindException) ex);
        }
        if (ex instanceof ConstraintViolationException) {
            return constraintViolationExceptionHandler((ConstraintViolationException) ex);
        }
        if (ex instanceof ValidationException) {
            return validationException((ValidationException) ex);
        }
        if (ex instanceof MaxUploadSizeExceededException) {
            return maxUploadSizeExceededExceptionHandler((MaxUploadSizeExceededException) ex);
        }
        if (ex instanceof NoHandlerFoundException) {
            return noHandlerFoundExceptionHandler((NoHandlerFoundException) ex);
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return httpRequestMethodNotSupportedExceptionHandler((HttpRequestMethodNotSupportedException) ex);
        }
        if (ex instanceof HttpMediaTypeNotSupportedException) {
            return httpMediaTypeNotSupportedExceptionHandler((HttpMediaTypeNotSupportedException) ex);
        }
        if (ex instanceof ServiceException) {
            return serviceExceptionHandler((ServiceException) ex);
        }
        if (ex instanceof AccessDeniedException) {
            return accessDeniedExceptionHandler(request, (AccessDeniedException) ex);
        }
        return defaultExceptionHandler(request, ex);
    }

    /**
     * Handles missing Spring MVC request parameters.
     *
     * For example, when an interface defines a parameter using @RequestParam("xx"),
     * but the request does not provide the "xx" parameter.
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public CommonResult<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException ex) {
        log.warn("[missingServletRequestParameterExceptionHandler]", ex);
        return CommonResult.error(
                BAD_REQUEST.getCode(),
                String.format("Missing request parameter: %s", ex.getParameterName())
        );
    }

    /**
     * Handles Spring MVC request parameter type mismatches.
     *
     * For example, when an interface defines a parameter using @RequestParam("xx") as Integer,
     * but the request provides a value of an incompatible type such as String.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public CommonResult<?> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException ex) {
        log.warn("[methodArgumentTypeMismatchExceptionHandler]", ex);
        return CommonResult.error(
                BAD_REQUEST.getCode(),
                String.format("Request parameter type mismatch: %s", ex.getMessage())
        );
    }

    /**
     * Handles invalid Spring MVC request parameters (validation errors).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> methodArgumentNotValidExceptionExceptionHandler(MethodArgumentNotValidException ex) {
        log.warn("[methodArgumentNotValidExceptionExceptionHandler]", ex);
        // Get errorMessage
        String errorMessage = null;
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError == null) {
            // Combine validation errors, reference: https://t.zsxq.com/3HVTx
            List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
            if (CollUtil.isNotEmpty(allErrors)) {
                errorMessage = allErrors.getFirst().getDefaultMessage();
            }
        } else {
            errorMessage = fieldError.getDefaultMessage();
        }
        // Convert to CommonResult
        if (StrUtil.isEmpty(errorMessage)) {
            return CommonResult.error(BAD_REQUEST);
        }
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("Invalid request parameters: %s", errorMessage));
    }

    /**
     * Handles incorrect Spring MVC parameter binding, which is essentially validated through Validator.
     */
    @ExceptionHandler(BindException.class)
    public CommonResult<?> bindExceptionHandler(BindException ex) {
        log.warn("[handleBindException]", ex);
        FieldError fieldError = ex.getFieldError();
        assert fieldError != null; // Assertion to avoid warnings
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("Invalid request parameters: %s", fieldError.getDefaultMessage()));
    }

    /**
     * Handles Spring MVC request parameter type mismatches.
     *
     * For example, when the xx field in an entity annotated with @RequestBody
     * is defined as Integer, but the request provides a String value for xx.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @SuppressWarnings("PatternVariableCanBeUsed")
    public CommonResult<?> methodArgumentTypeInvalidFormatExceptionHandler(HttpMessageNotReadableException ex) {
        log.warn("[methodArgumentTypeInvalidFormatExceptionHandler]", ex);
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) ex.getCause();
            return CommonResult.error(BAD_REQUEST.getCode(), String.format("Request parameter type mismatch: %s", invalidFormatException.getValue()));
        }
        if (StrUtil.startWith(ex.getMessage(), "Required request body is missing")) {
            return CommonResult.error(BAD_REQUEST.getCode(), "Request parameter type mismatch: request body is missing");
        }
        return defaultExceptionHandler(ServletUtils.getRequest(), ex);
    }

    /**
     * Handles exceptions thrown when Validator validation fails.
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public CommonResult<?> constraintViolationExceptionHandler(ConstraintViolationException ex) {
        log.warn("[constraintViolationExceptionHandler]", ex);
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("Invalid request parameters: %s", constraintViolation.getMessage()));
    }

    /**
     * Handles ValidationException thrown during local parameter validation in Dubbo Consumer.
     */
    @ExceptionHandler(value = ValidationException.class)
    public CommonResult<?> validationException(ValidationException ex) {
        log.warn("[constraintViolationExceptionHandler]", ex);
        // Cannot concatenate detailed error information because when Dubbo Consumer throws ValidationException,
        // it provides a plain string message that is not human-readable.
        return CommonResult.error(BAD_REQUEST);
    }

    /**
     * Handles exceptions caused by uploading files that are too large.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public CommonResult<?> maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException ex) {
        return CommonResult.error(BAD_REQUEST.getCode(), "Uploaded file is too large, please adjust and try again");
    }

    /**
     * Handles Spring MVC requests where the URL does not exist.
     *
     * Note: The following two configuration properties must be set:
     * 1. spring.mvc.throw-exception-if-no-handler-found = true
     * 2. spring.mvc.static-path-pattern = /statics/**
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public CommonResult<?> noHandlerFoundExceptionHandler(NoHandlerFoundException ex) {
        log.warn("[noHandlerFoundExceptionHandler]", ex);
        return CommonResult.error(
                NOT_FOUND.getCode(),
                String.format("Request URL does not exist: %s", ex.getRequestURL())
        );
    }

    /**
     * Handles Spring MVC requests with incorrect HTTP methods.
     *
     * For example, if an endpoint expects a GET request but receives a POST request,
     * causing a method mismatch.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResult<?> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException ex) {
        log.warn("[httpRequestMethodNotSupportedExceptionHandler]", ex);
        return CommonResult.error(
                METHOD_NOT_ALLOWED.getCode(),
                String.format("Incorrect request method: %s", ex.getMessage())
        );
    }

    /**
     * Handles Spring MVC requests with incorrect Content-Type.
     *
     * For example, if an endpoint expects Content-Type application/json but
     * receives application/octet-stream, causing a mismatch.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public CommonResult<?> httpMediaTypeNotSupportedExceptionHandler(HttpMediaTypeNotSupportedException ex) {
        log.warn("[httpMediaTypeNotSupportedExceptionHandler]", ex);
        return CommonResult.error(
                BAD_REQUEST.getCode(),
                String.format("Incorrect request Content-Type: %s", ex.getMessage())
        );
    }

    /**
     * Handles Spring Security exceptions when access is denied due to insufficient permissions.
     *
     * This occurs when using the @PreAuthorize annotation and AOP intercepts the request.
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public CommonResult<?> accessDeniedExceptionHandler(HttpServletRequest req, AccessDeniedException ex) {
        log.warn(
                "[accessDeniedExceptionHandler][userId({}) cannot access url({})]",
                WebFrameworkUtils.getLoginUserId(req),
                req.getRequestURL(),
                ex
        );
        return CommonResult.error(FORBIDDEN);
    }

    /**
     * Handles business exceptions (ServiceException).
     *
     * For example: insufficient product stock, or user phone number already exists.
     */
    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> serviceExceptionHandler(ServiceException ex) {
        // Only log when not included, to avoid excessive exception stack traces
        if (!IGNORE_ERROR_MESSAGES.contains(ex.getMessage())) {
            // Even when logging, only print the first StackTraceElement, and use WARN level in the console for better visibility
            try {
                StackTraceElement[] stackTraces = ex.getStackTrace();
                for (StackTraceElement stackTrace : stackTraces) {
                    if (ObjUtil.notEqual(stackTrace.getClassName(), ServiceExceptionUtil.class.getName())) {
                        log.warn("[serviceExceptionHandler]\n\t{}", stackTrace);
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Ignore logging to avoid affecting the main flow
            }
        }
        return CommonResult.error(ex.getCode(), ex.getMessage());
    }


    /**
     * Handles system exceptions as a global fallback for all unhandled errors.
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> defaultExceptionHandler(HttpServletRequest req, Throwable ex) {
        // Special case: if the exception is a ServiceException, return it directly.
        // For example: https://gitee.com/zhijiantianya/soar-cloud/issues/ICSSRM, https://gitee.com/zhijiantianya/soar-cloud/issues/ICT6FM
        if (ex.getCause() != null && ex.getCause() instanceof ServiceException) {
            return serviceExceptionHandler((ServiceException) ex.getCause());
        }

        // Case 1: handle table-not-exists exception
        CommonResult<?> tableNotExistsResult = handleTableNotExists(ex);
        if (tableNotExistsResult != null) {
            return tableNotExistsResult;
        }

        // Case 2: handle general exception
        log.error("[defaultExceptionHandler]", ex);
        // Insert exception log
        createExceptionLog(req, ex);
        // Return ERROR CommonResult
        return CommonResult.error(INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
    }

    private void createExceptionLog(HttpServletRequest req, Throwable e) {
        // Insert error log
        ApiErrorLogCreateReqDTO errorLog = new ApiErrorLogCreateReqDTO();
        try {
            // Initialize errorLog
            buildExceptionLog(errorLog, req, e);
            // Insert errorLog
            apiErrorLogApi.createApiErrorLogAsync(errorLog);
        } catch (Throwable th) {
            log.error("[createExceptionLog][url({}) log({}) exception occurred]", req.getRequestURI(),  JsonUtils.toJsonString(errorLog), th);
        }
    }

    private void buildExceptionLog(ApiErrorLogCreateReqDTO errorLog, HttpServletRequest request, Throwable e) {
        // 处理用户信息
        errorLog.setUserId(WebFrameworkUtils.getLoginUserId(request));
        errorLog.setUserType(WebFrameworkUtils.getLoginUserType(request));
        // 设置异常字段
        errorLog.setExceptionName(e.getClass().getName());
        errorLog.setExceptionMessage(ExceptionUtil.getMessage(e));
        errorLog.setExceptionRootCauseMessage(ExceptionUtil.getRootCauseMessage(e));
        errorLog.setExceptionStackTrace(ExceptionUtil.stacktraceToString(e));
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        Assert.notEmpty(stackTraceElements, "异常 stackTraceElements 不能为空");
        StackTraceElement stackTraceElement = stackTraceElements[0];
        errorLog.setExceptionClassName(stackTraceElement.getClassName());
        errorLog.setExceptionFileName(stackTraceElement.getFileName());
        errorLog.setExceptionMethodName(stackTraceElement.getMethodName());
        errorLog.setExceptionLineNumber(stackTraceElement.getLineNumber());
        // 设置其它字段
        errorLog.setTraceId(TracerUtils.getTraceId());
        errorLog.setApplicationName(applicationName);
        errorLog.setRequestUrl(request.getRequestURI());
        Map<String, Object> requestParams = MapUtil.<String, Object>builder()
                .put("query", ServletUtils.getParamMap(request))
                .put("body", ServletUtils.getBody(request)).build();
        errorLog.setRequestParams(JsonUtils.toJsonString(requestParams));
        errorLog.setRequestMethod(request.getMethod());
        errorLog.setUserAgent(ServletUtils.getUserAgent(request));
        errorLog.setUserIp(ServletUtils.getClientIP(request));
        errorLog.setExceptionTime(Instant.now());
    }

    /**
     * Handles the case where a database table does not exist.
     *
     * @param ex the exception
     * @return a CommonResult if it is a table-not-exists exception, otherwise null
     */
    private CommonResult<?> handleTableNotExists(Throwable ex) {
        String message = ExceptionUtil.getRootCauseMessage(ex);

        // Case 1: not a "table does not exist" error
        if (!message.contains("doesn't exist")) {
            return null;
        }

        // 1. Reporting module
        if (message.contains("report_")) {
            log.error("[Report module soar-module-report - schema not imported][see https://cloud.iocoder.cn/report/]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[Report module soar-module-report - schema not imported][see https://cloud.iocoder.cn/report/]");
        }

        // 2. Workflow module
        if (message.contains("bpm_")) {
            log.error("[Workflow module soar-module-bpm - schema not imported][see https://cloud.iocoder.cn/bpm/]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[Workflow module soar-module-bpm - schema not imported][see https://cloud.iocoder.cn/bpm/]");
        }

        // 3. WeChat Official Account module
        if (message.contains("mp_")) {
            log.error("[WeChat module soar-module-mp - schema not imported][see https://cloud.iocoder.cn/mp/build/]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[WeChat module soar-module-mp - schema not imported][see https://cloud.iocoder.cn/mp/build/]");
        }

        // 4. Mall system
        if (StrUtil.containsAny(message, "product_", "promotion_", "trade_")) {
            log.error("[Mall system soar-module-mall - disabled][see https://cloud.iocoder.cn/mall/build/]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[Mall system soar-module-mall - disabled][see https://cloud.iocoder.cn/mall/build/]");
        }

        // 5. ERP system
        if (message.contains("erp_")) {
            log.error("[ERP system soar-module-erp - schema not imported][see https://cloud.iocoder.cn/erp/build/]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[ERP system soar-module-erp - schema not imported][see https://cloud.iocoder.cn/erp/build/]");
        }

        // 6. CRM system
        if (message.contains("crm_")) {
            log.error("[CRM system soar-module-crm - schema not imported][see https://cloud.iocoder.cn/crm/build/]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[CRM system soar-module-crm - schema not imported][see https://cloud.iocoder.cn/crm/build/]");
        }

        // 7. Payment platform
        if (message.contains("pay_")) {
            log.error("[Payment module soar-module-pay - schema not imported][see https://cloud.iocoder.cn/pay/build/]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[Payment module soar-module-pay - schema not imported][see https://cloud.iocoder.cn/pay/build/]");
        }

        // 8. AI module
        if (message.contains("ai_")) {
            log.error("[AI module soar-module-ai - schema not imported][see https://cloud.iocoder.cn/ai/build/]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[AI module soar-module-ai - schema not imported][see https://cloud.iocoder.cn/ai/build/]");
        }

        // 9. IoT module
        if (message.contains("iot_")) {
            log.error("[IoT module soar-module-iot - schema not imported][see https://doc.iocoder.cn/iot/build/]");
            return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                    "[IoT module soar-module-iot - schema not imported][see https://doc.iocoder.cn/iot/build/]");
        }

        return null;
    }
}
