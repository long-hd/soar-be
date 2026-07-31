package com.hdl.soar.framework.mq.redis.core.pubsub;

import cn.hutool.core.util.TypeUtil;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.mq.redis.core.RedisMQTemplate;
import com.hdl.soar.framework.mq.redis.core.interceptor.RedisMessageInterceptor;
import com.hdl.soar.framework.mq.redis.core.message.AbstractRedisMessage;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Base class for Pub/Sub consumers, providing broadcast (at-most-once) delivery.
 *
 * <p>Every listener connected at publish time receives its own copy of the message.
 * There is no acknowledgement or persistence: a message published while a listener
 * is offline is not delivered to it.
 *
 * @param <T> the message type; the generic argument is required, as it is used to
 *            resolve both the channel and the deserialization target.
 */
public abstract class AbstractRedisChannelMessageListener<T extends AbstractRedisChannelMessage>
        implements MessageListener {

    private final Class<T> messageType;
    private final String channel;

    @Setter
    private RedisMQTemplate redisMQTemplate;

    @SneakyThrows
    protected AbstractRedisChannelMessageListener() {
        this.messageType = resolveMessageType();
        this.channel = messageType.getDeclaredConstructor().newInstance().getChannel();
    }

    /**
     * @return the channel this listener subscribes to.
     */
    public final String getChannel() {
        return channel;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        T messageObj = JsonUtils.parseObject(message.getBody(), messageType);
        try {
            consumeMessageBefore(messageObj);
            onMessage(messageObj);
        } finally {
            consumeMessageAfter(messageObj);
        }
    }

    /**
     * Handles a message.
     *
     * @param message the message to handle
     */
    public abstract void onMessage(T message);

    @SuppressWarnings("unchecked")
    private Class<T> resolveMessageType() {
        Type type = TypeUtil.getTypeArgument(getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format(
                    "Listener %s must declare its message type as a generic argument", getClass().getName()));
        }
        return (Class<T>) type;
    }

    private void consumeMessageBefore(AbstractRedisMessage message) {
        List<RedisMessageInterceptor> interceptors = redisMQTemplate.getInterceptors();
        interceptors.forEach(interceptor -> interceptor.consumeMessageBefore(message));
    }

    private void consumeMessageAfter(AbstractRedisMessage message) {
        List<RedisMessageInterceptor> interceptors = redisMQTemplate.getInterceptors();
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).consumeMessageAfter(message);
        }
    }

}
