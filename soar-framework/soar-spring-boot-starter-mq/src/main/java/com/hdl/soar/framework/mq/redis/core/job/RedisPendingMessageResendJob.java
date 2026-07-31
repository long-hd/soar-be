package com.hdl.soar.framework.mq.redis.core.job;

import cn.hutool.core.collection.CollUtil;
import com.hdl.soar.framework.mq.redis.core.RedisMQTemplate;
import com.hdl.soar.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Redelivers stream messages left pending by a consumer that stopped before
 * acknowledging them.
 *
 * <p>Each run scans the pending list of every stream and group. A message idle for
 * longer than {@link #EXPIRE_TIME} is re-appended to the stream and the original
 * entry is acknowledged. A distributed lock ensures only one node runs per cycle.
 *
 * <p><b>Known limitation.</b> Because a persistently failing message is re-appended
 * as a new entry each cycle, its delivery count resets and it is retried
 * indefinitely. Bounding retries requires reclaiming the same entry with
 * {@code XCLAIM}/{@code XAUTOCLAIM} so its delivery count accumulates, then routing
 * it to a dead-letter stream once a threshold is exceeded. This is not yet implemented.
 */
@Slf4j
@AllArgsConstructor
public class RedisPendingMessageResendJob {

    public static final String DEFAULT_RESEND_LOCK_KEY = "redis:stream:pending-message-resend:lock";

    /**
     * Minimum idle time before a pending message is redelivered, in seconds.
     */
    private static final int EXPIRE_TIME = 5 * 60;

    private final List<AbstractRedisStreamMessageListener<?>> listeners;
    private final RedisMQTemplate redisMQTemplate;
    private final RedissonClient redissonClient;
    private final String resendLockKey;

    /**
     * Runs at second 35 of every minute, offset from the top of the minute to avoid
     * contending with other scheduled tasks.
     */
    @Scheduled(cron = "35 * * * * ?")
    public void messageResend() {
        RLock lock = redissonClient.getLock(resendLockKey);
        if (!lock.tryLock()) {
            log.debug("[messageResend][lock held by another node, skipping][key={}]", resendLockKey);
            return;
        }
        try {
            execute();
        } catch (Exception ex) {
            log.error("[messageResend][failed][key={}]", resendLockKey, ex);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void execute() {
        StreamOperations<String, Object, Object> ops = redisMQTemplate.getRedisTemplate().opsForStream();
        for (AbstractRedisStreamMessageListener<?> listener : listeners) {
            PendingMessagesSummary summary = Objects.requireNonNull(
                    ops.pending(listener.getStreamKey(), listener.getGroup()));
            Map<String, Long> perConsumer = summary.getPendingMessagesPerConsumer();
            perConsumer.forEach((consumerName, count) -> {
                if (count == 0) {
                    return;
                }
                log.info("[messageResend][consumer={} pending={}]", consumerName, count);
                PendingMessages pendings = ops.pending(listener.getStreamKey(),
                        Consumer.from(listener.getGroup(), consumerName), Range.unbounded(), count);
                for (PendingMessage pending : pendings) {
                    if (pending.getElapsedTimeSinceLastDelivery().getSeconds() < EXPIRE_TIME) {
                        continue;
                    }
                    List<MapRecord<String, Object, Object>> records = ops.range(listener.getStreamKey(),
                            Range.of(Range.Bound.inclusive(pending.getIdAsString()),
                                    Range.Bound.inclusive(pending.getIdAsString())));
                    if (CollUtil.isEmpty(records)) {
                        // Entry already trimmed by the cleanup job; acknowledge to clear it from the pending list.
                        ops.acknowledge(listener.getStreamKey(), listener.getGroup(), pending.getIdAsString());
                        continue;
                    }
                    ops.add(StreamRecords.newRecord()
                            .ofObject(records.getFirst().getValue())
                            .withStreamKey(listener.getStreamKey()));
                    ops.acknowledge(listener.getGroup(), records.getFirst());
                    log.info("[messageResend][redelivered id={}]", records.getFirst().getId());
                }
            });
        }
    }

}
