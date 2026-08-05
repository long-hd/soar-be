package com.hdl.soar.module.pay.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.hdl.soar.module.pay.dal.redis.RedisKeyConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Generates unique payment numbers via a per-second Redis counter.
 * <p>
 * The number is {@code prefix + yyyyMMddHHmmss + counter}, where the counter resets each second
 * (the key expires after one minute). Used as the external order number ({@code no}) for each
 * order-extension, which must be unique per attempt.
 */
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayNoRedisDAO {

    StringRedisTemplate stringRedisTemplate;

    /**
     * Generate a payment number.
     *
     * @param prefix number prefix (e.g. "P")
     * @return a unique payment number
     */
    public String generate(String prefix) {
        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN);
        String key = RedisKeyConstants.PAY_NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, Duration.ofMinutes(1L));
        return noPrefix + no;
    }

}
