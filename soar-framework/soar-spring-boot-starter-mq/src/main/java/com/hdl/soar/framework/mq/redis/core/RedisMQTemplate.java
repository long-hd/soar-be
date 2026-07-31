package com.hdl.soar.framework.mq.redis.core;

import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.mq.redis.core.interceptor.RedisMessageInterceptor;
import com.hdl.soar.framework.mq.redis.core.message.AbstractRedisMessage;
import com.hdl.soar.framework.mq.redis.core.pubsub.AbstractRedisChannelMessage;
import com.hdl.soar.framework.mq.redis.core.stream.AbstractRedisStreamMessage;
import lombok.Getter;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for producing Redis messages.
 *
 * <p>Provides two {@code send} overloads: one for Pub/Sub broadcast and one for
 * Stream. Registered {@link RedisMessageInterceptor}s run around each send, in
 * order before the send and in reverse order after it.
 *
 * @implNote The backing template must serialize values as plain strings (i.e. a
 * {@code StringRedisTemplate}), because the payload is already JSON-encoded here
 * via {@link JsonUtils}. A JSON-serializing template would encode it twice.
 */
@Getter
public class RedisMQTemplate {

    private final RedisTemplate<String, ?> redisTemplate;

    private final List<RedisMessageInterceptor> interceptors = new ArrayList<>();

    public RedisMQTemplate(RedisTemplate<String, ?> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Publishes a message over Redis Pub/Sub. The message is not persisted and is
     * received only by subscribers connected at publish time.
     *
     * @param message the message to publish
     * @param <T>     the channel message type
     */
    public <T extends AbstractRedisChannelMessage> void send(T message) {
        try {
            sendMessageBefore(message);
            redisTemplate.convertAndSend(message.getChannel(), JsonUtils.toJsonString(message));
        } finally {
            sendMessageAfter(message);
        }
    }

    /**
     * Appends a message to a Redis Stream. The message persists in the stream
     * until consumed and acknowledged.
     *
     * @param message the message to append
     * @param <T>     the stream message type
     * @return the id of the appended stream entry
     */
    public <T extends AbstractRedisStreamMessage> RecordId send(T message) {
        try {
            sendMessageBefore(message);
            return redisTemplate.opsForStream().add(StreamRecords.newRecord()
                    .ofObject(JsonUtils.toJsonString(message))
                    .withStreamKey(message.getStreamKey()));
        } finally {
            sendMessageAfter(message);
        }
    }

    /**
     * @param interceptor interceptor to register
     */
    public void addInterceptor(RedisMessageInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    private void sendMessageBefore(AbstractRedisMessage message) {
        interceptors.forEach(interceptor -> interceptor.sendMessageBefore(message));
    }

    private void sendMessageAfter(AbstractRedisMessage message) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).sendMessageAfter(message);
        }
    }

}
