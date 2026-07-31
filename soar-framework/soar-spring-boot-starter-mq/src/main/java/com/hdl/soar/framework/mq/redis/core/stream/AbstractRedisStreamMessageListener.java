package com.hdl.soar.framework.mq.redis.core.stream;


import cn.hutool.core.util.TypeUtil;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.mq.redis.core.RedisMQTemplate;
import com.hdl.soar.framework.mq.redis.core.interceptor.RedisMessageInterceptor;
import com.hdl.soar.framework.mq.redis.core.message.AbstractRedisMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Base class for Stream consumers, providing at-least-once delivery via manual
 * acknowledgement.
 *
 * <p>For each record the container delivers, this class deserializes the payload,
 * invokes {@link #onMessage(AbstractRedisStreamMessage)}, and acknowledges the
 * record only after that call returns normally. If the call throws, the record is
 * left unacknowledged and remains pending for later redelivery.
 *
 * @param <T> the message type; the generic argument is required, as it is used to
 *            resolve both the stream key and the deserialization target.
 */
public abstract class AbstractRedisStreamMessageListener<T extends AbstractRedisStreamMessage>
        implements StreamListener<String, ObjectRecord<String, String>> {

    private final Class<T> messageType;

    @Getter
    private final String streamKey;

    /**
     * Consumer group name, defaulting to {@code spring.application.name}. Instances
     * sharing a group share the workload; distinct groups each receive every message.
     */
    @Value("${spring.application.name}")
    @Getter
    private String group;

    @Setter
    private RedisMQTemplate redisMQTemplate;

    @SneakyThrows
    protected AbstractRedisStreamMessageListener() {
        this.messageType = resolveMessageType();
        this.streamKey = messageType.getDeclaredConstructor().newInstance().getStreamKey();
    }

    @Override
    public void onMessage(ObjectRecord<String, String> record) {
        T message = JsonUtils.parseObject(record.getValue(), messageType);
        try {
            consumeMessageBefore(message);
            onMessage(message);
            // Acknowledge only after successful handling; this is what makes delivery at-least-once.
            redisMQTemplate.getRedisTemplate().opsForStream().acknowledge(group, record);
        } finally {
            consumeMessageAfter(message);
        }
    }

    /**
     * Handles a message.
     *
     * @param message the message to handle
     * @implSpec Implementations must be idempotent, since at-least-once delivery
     * means a message may be handled more than once.
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