package com.hdl.soar.framework.tenant.core.job;

import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.tenant.core.service.TenantFrameworkService;
import com.hdl.soar.framework.tenant.core.util.TenantUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AOP aspect that runs a {@link TenantJob} method once per tenant.
 *
 * <p>Flow: fetch all tenant ids → for each, run the job body inside
 * {@link TenantUtils#execute(Long, java.util.concurrent.Callable)}, which sets the tenant
 * context before and restores it after.
 *
 * <p>Iteration is sequential (not parallel) so that ordering and per-tenant logging stay
 * deterministic and exceptions are easy to attribute.
 *
 * <p>Failure isolation: an exception in one tenant is caught and recorded, then the remaining
 * tenants still run — one broken tenant must not block the rest.
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class TenantJobAspect {

    private final TenantFrameworkService tenantFrameworkService;

    @Around("@annotation(tenantJob)")
    public Object around(ProceedingJoinPoint joinPoint, TenantJob tenantJob) throws Throwable {
        List<Long> tenantIds = tenantFrameworkService.getTenantIds();
        if (tenantIds == null || tenantIds.isEmpty()) {
            log.info("[TenantJob][No tenants found, skipping job]");
            return null;
        }

        // Collect each tenant's outcome; LinkedHashMap keeps tenant order stable in the result.
        Map<Long, Object> results = new LinkedHashMap<>();
        tenantIds.forEach(tenantId ->
                TenantUtils.execute(tenantId, () -> {
                    try {
                        // proceed() = the actual job body, now running with tenant context set
                        results.put(tenantId, joinPoint.proceed());
                    } catch (Throwable e) {
                        // Isolate the failure: record it and let the other tenants continue.
                        results.put(tenantId, "job failed: " + e.getMessage());
                        log.error("[TenantJob][tenantId({}) execution failed]", tenantId, e);
                    }
                    return null;
                }));

        // Returned to the job engine, which stores it as the job's result/log.
        return JsonUtils.toJsonString(results);
    }

}
