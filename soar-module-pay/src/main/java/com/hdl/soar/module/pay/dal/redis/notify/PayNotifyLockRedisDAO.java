package com.hdl.soar.module.pay.dal.redis.notify;

import com.hdl.soar.module.pay.dal.redis.RedisKeyConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

/**
 * Per-task distributed lock. The afterCommit fast-path and the poll job (possibly on different nodes)
 * can both pick up the same task; the lock ensures only one delivery attempt runs at a time. A caller
 * that fails to acquire the lock simply skips — the holder is already handling it.
 */
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayNotifyLockRedisDAO {

    RedissonClient redissonClient;

    public void lock(Long taskId, Runnable runnable) {
        RLock lock = redissonClient.getLock(RedisKeyConstants.PAY_NOTIFY_LOCK + taskId);
        if (!lock.tryLock()) {
            return; // another thread/node is delivering this task
        }
        try {
            runnable.run();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
