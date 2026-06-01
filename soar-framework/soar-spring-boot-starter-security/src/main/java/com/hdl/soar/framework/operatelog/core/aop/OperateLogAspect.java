package com.hdl.soar.framework.operatelog.core.aop;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.biz.system.logger.OperateLogCommonApi;
import com.hdl.soar.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.common.util.monitor.TracerUtils;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.framework.operatelog.core.annotation.OperateLog;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * AOP Aspect for {@link com.hdl.soar.framework.operatelog.core.annotation.OperateLog} annotation.
 * <p>
 * Intercepts annotated service methods, resolves SpEL expressions from
 * method parameters and return value, then delegates log creation to
 * {@link OperateLogCommonApi} asynchronously.
 *
 * @author hdl
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class OperateLogAspect {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private final OperateLogCommonApi operateLogApi;

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        // 0. Check enable
        if (!operateLog.enable()) {
            return joinPoint.proceed();
        }

        // 1. Execute the target method
        Object result = null;
        Throwable exception = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            exception = ex;
            throw ex;
        } finally {
            // 2. Record operate log (even if exception occurred)
            try {
                recordLog(joinPoint, operateLog, result, exception);
            } catch (Throwable ex) {
                log.error("[around][method({}) operate log recording failed]",
                        joinPoint.getSignature().toShortString(), ex);
            }
        }
        return result;
    }

    private void recordLog(ProceedingJoinPoint joinPoint, OperateLog annotation,
                           Object result, Throwable exception) {
        // 1. Build SpEL context
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        EvaluationContext spelContext = buildSpelContext(method, joinPoint.getArgs(), result);

        // 2. Build DTO
        OperateLogCreateReqDTO dto = new OperateLogCreateReqDTO();

        // 2.1 User info
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser != null) {
            dto.setUserId(loginUser.getId());
            dto.setUserType(UserTypeEnum.of(loginUser.getUserType()));
        } else {
            // Skip logging if no user context (e.g., system job)
            log.debug("[recordLog][method({}) skipped: no login user]",
                    joinPoint.getSignature().toShortString());
            return;
        }

        // 2.2 Module info
        dto.setModule(annotation.module());
        dto.setName(annotation.name());

        // 2.3 Resolve SpEL: bizId
        if (StrUtil.isNotBlank(annotation.bizId())) {
            Object bizIdValue = resolveExpression(spelContext, annotation.bizId());
            dto.setBizId(toBizId(bizIdValue));
        }

        // 2.4 Resolve SpEL: content
        if (StrUtil.isNotBlank(annotation.content())) {
            Object contentValue = resolveExpression(spelContext, annotation.content());
            dto.setContent(contentValue != null ? contentValue.toString() : null);
        } else {
            // Default content: "module - name"
            dto.setContent(annotation.module() + " - " + annotation.name());
        }

        // 2.5 Resolve SpEL: extra
        if (StrUtil.isNotBlank(annotation.extra())) {
            Object extraValue = resolveExpression(spelContext, annotation.extra());
            dto.setExtra(extraValue != null ? JsonUtils.toJsonString(extraValue) : null);
        }

        // 2.6 Trace
        dto.setTraceId(TracerUtils.getTraceId());

        // 2.7 Request info
        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null) {
            dto.setRequestMethod(request.getMethod());
            dto.setRequestUrl(request.getRequestURI());
            dto.setUserIp(ServletUtils.getClientIP(request));
            dto.setUserAgent(ServletUtils.getUserAgent(request));
        }

        // 3. Async persist
        operateLogApi.createOperateLogAsync(dto);
    }

    // ========== SpEL resolution ==========

    private EvaluationContext buildSpelContext(Method method, Object[] args, Object result) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        // Register method parameters by name
        String[] paramNames = NAME_DISCOVERER.getParameterNames(method);
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        // Register return value as #result
        if (result != null) {
            context.setVariable("result", result);
        }
        return context;
    }

    private Object resolveExpression(EvaluationContext context, String expression) {
        try {
            return PARSER.parseExpression(expression).getValue(context);
        } catch (Exception ex) {
            log.warn("[resolveExpression][expression({}) resolution failed: {}]", expression, ex.getMessage());
            return null;
        }
    }

    private Long toBizId(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case Long longVal -> {
                return longVal;
            }
            case Number number -> {
                return number.longValue();
            }
            default -> {
            }
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            log.warn("[toBizId][value({}) cannot be parsed to Long]", value);
            return null;
        }
    }


}
