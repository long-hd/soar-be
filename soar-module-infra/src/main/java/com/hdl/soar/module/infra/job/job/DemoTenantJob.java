package com.hdl.soar.module.infra.job.job;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.quartz.core.handler.JobHandler;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.tenant.core.job.TenantJob;
import com.hdl.soar.module.infra.dal.postgres.logger.ApiAccessLogRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demonstrates {@link TenantJob}: the body runs once per tenant, each under that tenant's
 * context. The access-log table is tenant-scoped, so the count differs per tenant — proving
 * the tenant filter is actually applied on a background thread (where nothing sets the tenant
 * for us).
 *
 * <p>This is a demonstration/acceptance job, not a production task.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DemoTenantJob implements JobHandler {

    ApiAccessLogRepository apiAccessLogRepository;

    @Override
    @TenantJob // runs once per tenant
    public String execute(String param) throws Exception {
        Long tenantId = TenantContextHolder.getTenantId(); // set by the aspect, per tenant
        long count = apiAccessLogRepository.count();        // tenant-filtered by @TenantId
        log.info("[DemoTenantJob][tenant({}) has {} access-log rows]", tenantId, count);
        return StrUtil.format("tenant={}, accessLogCount={}", tenantId, count);
    }

}
