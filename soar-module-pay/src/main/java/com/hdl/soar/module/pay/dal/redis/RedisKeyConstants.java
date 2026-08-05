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

}