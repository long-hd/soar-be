package com.hdl.soar.framework.tenant.core.aop;

import com.hdl.soar.framework.common.util.spring.SpringExpressionUtils;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * AOP aspect that temporarily disables tenant filtering for methods
 * annotated with {@link TenantIgnore}.
 *
 * <p>Saves and restores the previous ignore state, so nested calls
 * (e.g. {@code @TenantIgnore} method calling another) work correctly.
 *
 * <p>Logically equivalent to wrapping the method body with
 * {@link com.hdl.soar.framework.tenant.core.util.TenantUtils#executeIgnore(Runnable)}.
 */
@Slf4j
@Aspect
public class TenantIgnoreAspect {

    @Around("@annotation(tenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint, TenantIgnore tenantIgnore) throws Throwable {
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            // Evaluate SpEL condition — only ignore when expression returns true
            Object enable = SpringExpressionUtils.parseExpression(joinPoint, tenantIgnore.enable());
            if (Boolean.TRUE.equals(enable)) {
                TenantContextHolder.setIgnore(true);
            }
            return joinPoint.proceed();
        } finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

}
