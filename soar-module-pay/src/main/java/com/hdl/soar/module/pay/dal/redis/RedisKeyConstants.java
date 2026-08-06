package com.hdl.soar.module.pay.dal.redis;

/**
 * Pay module Redis key enumeration.
 */
public interface RedisKeyConstants {

    /**
     * Sequence counter for generating payment numbers.
     * <ul>
     *  <li>KEY format: pay_no:{prefix + yyyyMMddHHmmss}</li>
     *  <li>VALUE type: String (incrementing counter)</li>
     * </ul>
     */
    String PAY_NO = "pay_no:";

    /**
     * Per-task notify delivery lock.
     * <ul>
     *  <li>KEY format: pay_notify_lock:{taskId}</li>
     *  <li>TYPE: Redisson lock</li>
     * </ul>
     */
    String PAY_NOTIFY_LOCK = "pay_notify_lock:";

}