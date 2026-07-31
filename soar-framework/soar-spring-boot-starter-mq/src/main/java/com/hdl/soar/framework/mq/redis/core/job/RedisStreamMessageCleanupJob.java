package com.hdl.soar.framework.mq.redis.core.job;

import com.hdl.soar.framework.mq.redis.core.RedisMQTemplate;
import com.hdl.soar.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Trims each stream to a bounded length.
 *
 * <p>A stream retains entries after they are acknowledged, so without trimming it
 * grows without bound. Each run caps every stream at {@link #MAX_COUNT} entries. A
 * distributed lock ensures only one node runs per cycle.
 */
@Slf4j
@AllArgsConstructor
public class RedisStreamMessageCleanupJob {

    public static final String DEFAULT_CLEANUP_LOCK_KEY = "redis:stream:message-cleanup:lock";

    /**
     * Maximum number of entries retained per stream.
     */
    private static final long MAX_COUNT = 10_000;

    private final List<AbstractRedisStreamMessageListener<?>> listeners;
    private final RedisMQTemplate redisMQTemplate;
    private final RedissonClient redissonClient;
    private final String cleanupLockKey;

    /**
     * Runs at the top of every hour.
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanup() {
        RLock lock = redissonClient.getLock(cleanupLockKey);
        if (!lock.tryLock()) {
            log.debug("[cleanup][lock held by another node, skipping][key={}]", cleanupLockKey);
            return;
        }
        try {
            execute();
        } catch (Exception ex) {
            log.error("[cleanup][failed][key={}]", cleanupLockKey, ex);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void execute() {
        StreamOperations<String, Object, Object> ops = redisMQTemplate.getRedisTemplate().opsForStream();
        for (AbstractRedisStreamMessageListener<?> listener : listeners) {
            try {
                // Exact trim (approximate=false) to keep the stream at the cap.
                Long trimmed = ops.trim(listener.getStreamKey(), MAX_COUNT, false);
                if (trimmed != null && trimmed > 0) {
                    log.info("[cleanup][stream={} trimmed={}]", listener.getStreamKey(), trimmed);
                }
            } catch (Exception ex) {
                log.error("[cleanup][stream={} failed]", listener.getStreamKey(), ex);
            }
        }
    }

}
