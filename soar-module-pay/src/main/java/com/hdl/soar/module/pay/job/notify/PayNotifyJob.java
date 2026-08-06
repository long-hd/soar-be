package com.hdl.soar.module.pay.job.notify;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.quartz.core.handler.JobHandler;
import com.hdl.soar.framework.tenant.core.job.TenantJob;
import com.hdl.soar.module.pay.service.notify.PayNotifyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * Poll relay for the notify outbox. Runs once per tenant (so tenant-filtered queries see the right
 * rows), dispatching every due WAITING task. This is the reliability backstop behind the afterCommit
 * fast-path; together they give at-least-once delivery with retry.
 * <p>
 * Register it as a Quartz job (bean name {@code payNotifyJob}) on a short interval, e.g. every 30–60s.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayNotifyJob implements JobHandler {

    PayNotifyService payNotifyService;

    @Override
    @TenantJob // once per tenant
    public String execute(String param) throws Exception {
        int dispatched = payNotifyService.executeNotify();
        return StrUtil.format("dispatched={}", dispatched);
    }

}
