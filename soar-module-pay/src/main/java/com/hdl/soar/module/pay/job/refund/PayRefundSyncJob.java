package com.hdl.soar.module.pay.job.refund;

import com.hdl.soar.framework.quartz.core.handler.JobHandler;
import com.hdl.soar.module.pay.service.refund.PayRefundService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * Reconcile WAITING refunds by re-querying the channel. GLOBAL (NOT {@code @TenantJob}): pay_refund
 * is a global table, so a per-tenant job would re-scan the same rows once per tenant.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayRefundSyncJob implements JobHandler {

    PayRefundService refundService;

    @Override
    public String execute(String param) {
        int resolved = refundService.syncRefund();
        return "resolved=" + resolved;
    }

}
