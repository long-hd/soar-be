package com.hdl.soar.framework.tenant.core.mq.redis;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.mq.redis.core.interceptor.RedisMessageInterceptor;
import com.hdl.soar.framework.mq.redis.core.message.AbstractRedisMessage;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;

import static com.hdl.soar.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * Propagates the current tenant across Redis MQ messages.
 *
 * <p>On send, the tenant from {@link TenantContextHolder} is written into the
 * message headers. On consume, it is restored into {@link TenantContextHolder}
 * before the message is handled and cleared afterwards, so data access inside the
 * handler is scoped to the tenant that produced the message.
 */
public class TenantRedisMessageInterceptor implements RedisMessageInterceptor {

    @Override
    public void sendMessageBefore(AbstractRedisMessage message) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            message.addHeader(HEADER_TENANT_ID, tenantId.toString());
        }
    }

    @Override
    public void consumeMessageBefore(AbstractRedisMessage message) {
        String tenantId = message.getHeader(HEADER_TENANT_ID);
        if (StrUtil.isNotEmpty(tenantId)) {
            TenantContextHolder.setTenantId(Long.valueOf(tenantId));
        }
    }

    @Override
    public void consumeMessageAfter(AbstractRedisMessage message) {
        // The consumer runs on a pooled thread with no inherited tenant context,
        // so clear unconditionally to avoid leaking into the next message.
        TenantContextHolder.clear();
    }

}
