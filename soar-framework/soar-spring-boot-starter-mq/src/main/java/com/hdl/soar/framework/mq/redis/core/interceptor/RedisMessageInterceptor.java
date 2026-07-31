package com.hdl.soar.framework.mq.redis.core.interceptor;

import com.hdl.soar.framework.mq.redis.core.message.AbstractRedisMessage;

/**
 * Extension hook applied around message send and consume.
 *
 * <p>Interceptors are invoked by {@link com.hdl.soar.framework.mq.redis.core.RedisMQTemplate}
 * when sending and by the listener base classes when consuming. The primary use
 * case is multi-tenancy: writing the current tenant into the message headers
 * before sending, and restoring it from the headers before consuming.
 *
 * <p>All methods are no-op by default; implementations override only what they need.
 */
public interface RedisMessageInterceptor {

    /** @param message the message about to be sent */
    default void sendMessageBefore(AbstractRedisMessage message) {
    }

    /** @param message the message that has been sent */
    default void sendMessageAfter(AbstractRedisMessage message) {
    }

    /** @param message the message about to be consumed */
    default void consumeMessageBefore(AbstractRedisMessage message) {
    }

    /** @param message the message that has been consumed */
    default void consumeMessageAfter(AbstractRedisMessage message) {
    }

}
