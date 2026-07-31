package com.hdl.soar.framework.mq.redis.config;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.hdl.soar.framework.mq.redis.core.RedisMQTemplate;
import com.hdl.soar.framework.mq.redis.core.job.RedisPendingMessageResendJob;
import com.hdl.soar.framework.mq.redis.core.job.RedisStreamMessageCleanupJob;
import com.hdl.soar.framework.mq.redis.core.pubsub.AbstractRedisChannelMessageListener;
import com.hdl.soar.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import com.hdl.soar.framework.redis.config.SoarRedisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Properties;

/**
 * Registers the consumer-side infrastructure: the Pub/Sub and Stream listener
 * containers and the two stream maintenance jobs.
 *
 * <p>Each bean is conditional on a matching listener being present, so nothing is
 * created for a messaging style that is not used. {@code @EnableScheduling} is
 * declared here so the maintenance jobs run without depending on any other starter.
 */
@Slf4j
@EnableScheduling
@AutoConfiguration(after = SoarRedisAutoConfiguration.class)
public class SoarRedisMQConsumerAutoConfiguration {

    /**
     * Registers the container that dispatches Pub/Sub messages to channel listeners.
     *
     * @param redisMQTemplate the producing template, shared with listeners
     * @param listeners       the channel listeners to register
     * @return the container
     */
    @Bean
    @ConditionalOnBean(AbstractRedisChannelMessageListener.class)
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractRedisChannelMessageListener<?>> listeners) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisMQTemplate.getRedisTemplate().getRequiredConnectionFactory());
        for (AbstractRedisChannelMessageListener<?> listener : listeners) {
            listener.setRedisMQTemplate(redisMQTemplate);
            container.addMessageListener(listener, new ChannelTopic(listener.getChannel()));
            log.info("[redisMessageListenerContainer][registered channel={} listener={}]",
                    listener.getChannel(), listener.getClass().getName());
        }
        return container;
    }

    /**
     * @param listeners       the stream listeners whose pending lists are scanned
     * @param redisMQTemplate the template providing stream operations
     * @param redissonClient  provides the distributed lock
     * @return the redelivery job
     */
    @Bean
    @ConditionalOnBean(AbstractRedisStreamMessageListener.class)
    public RedisPendingMessageResendJob redisPendingMessageResendJob(
            List<AbstractRedisStreamMessageListener<?>> listeners,
            RedisMQTemplate redisMQTemplate, RedissonClient redissonClient) {
        return new RedisPendingMessageResendJob(listeners, redisMQTemplate, redissonClient,
                RedisPendingMessageResendJob.DEFAULT_RESEND_LOCK_KEY);
    }

    /**
     * @param listeners       the stream listeners whose streams are trimmed
     * @param redisMQTemplate the template providing stream operations
     * @param redissonClient  provides the distributed lock
     * @return the cleanup job
     */
    @Bean
    @ConditionalOnBean(AbstractRedisStreamMessageListener.class)
    public RedisStreamMessageCleanupJob redisStreamMessageCleanupJob(
            List<AbstractRedisStreamMessageListener<?>> listeners,
            RedisMQTemplate redisMQTemplate, RedissonClient redissonClient) {
        return new RedisStreamMessageCleanupJob(listeners, redisMQTemplate, redissonClient,
                RedisStreamMessageCleanupJob.DEFAULT_CLEANUP_LOCK_KEY);
    }

    /**
     * Registers the Stream listener container: creates a consumer group per stream
     * and subscribes each listener as a consumer within its group.
     *
     * @param redisMQTemplate the producing template, shared with listeners
     * @param listeners       the stream listeners to register
     * @return the started container
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnBean(AbstractRedisStreamMessageListener.class)
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisStreamMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractRedisStreamMessageListener<?>> listeners) {
        RedisTemplate<String, ?> redisTemplate = redisMQTemplate.getRedisTemplate();
        checkRedisVersion(redisTemplate);

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(10) // max records fetched per read
                        .targetType(String.class) // deserialization is done in the listener base class
                        .build();
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container =
                StreamMessageListenerContainer.create(redisTemplate.getRequiredConnectionFactory(), options);

        String consumerName = buildConsumerName();
        // Registered sequentially: startup registration gains nothing from parallelism.
        for (AbstractRedisStreamMessageListener<?> listener : listeners) {
            try {
                redisTemplate.opsForStream().createGroup(listener.getStreamKey(), listener.getGroup());
            } catch (Exception ignore) {
                // Group already exists (or stream absent); safe to ignore across concurrent node startups.
            }
            listener.setRedisMQTemplate(redisMQTemplate);
            Consumer consumer = Consumer.from(listener.getGroup(), consumerName);
            // lastConsumed: deliver only messages not yet dispatched to this group.
            StreamOffset<String> offset = StreamOffset.create(listener.getStreamKey(), ReadOffset.lastConsumed());
            StreamMessageListenerContainer.StreamReadRequest<String> request =
                    StreamMessageListenerContainer.StreamReadRequest.builder(offset)
                            .consumer(consumer)
                            .autoAcknowledge(false) // manual ack; enables at-least-once
                            .cancelOnError(throwable -> false) // one failed message must not cancel the subscription
                            .build();
            container.register(request, listener);
            log.info("[redisStreamMessageListenerContainer][registered stream={} listener={}]",
                    listener.getStreamKey(), listener.getClass().getName());
        }
        return container;
    }

    /**
     * @return a consumer name unique per process, formatted as {@code host@pid}.
     */
    public static String buildConsumerName() {
        return String.format("%s@%d", SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID());
    }

    /**
     * Verifies the Redis server is at least version 5.0, which Streams require.
     *
     * @param redisTemplate template used to query server info
     * @throws IllegalStateException if the version is below 5.0
     */
    public static void checkRedisVersion(RedisTemplate<String, ?> redisTemplate) {
        Properties info = redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
        String version = MapUtil.getStr(info, "redis_version");
        int major = Integer.parseInt(StrUtil.subBefore(version, '.', false));
        if (major < 5) {
            throw new IllegalStateException(StrUtil.format(
                    "Redis version {} is below the required minimum of 5.0.0 for Streams.", version));
        }
    }

}
