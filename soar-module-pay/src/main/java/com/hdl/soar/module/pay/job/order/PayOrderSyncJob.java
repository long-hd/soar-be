package com.hdl.soar.module.pay.job.order;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.quartz.core.handler.JobHandler;
import com.hdl.soar.module.pay.service.order.PayOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * Reconcile job. Global (no {@code @TenantJob}): it scans the GLOBAL order/extension tables, so a
 * per-tenant run would re-scan the same rows once per tenant. The only tenant-scoped access (loading
 * the channel) is handled per-attempt inside the service via {@code executeIgnore}. Runs frequently,
 * e.g. every 60s.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOrderSyncJob implements JobHandler {

    PayOrderService payOrderService;

    @Override
    public String execute(String param) throws Exception {
        int recovered = payOrderService.syncOrder();
        return StrUtil.format("recovered={}", recovered);
    }

}
