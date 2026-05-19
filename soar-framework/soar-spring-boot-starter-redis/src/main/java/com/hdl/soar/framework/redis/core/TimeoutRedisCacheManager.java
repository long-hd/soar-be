package com.hdl.soar.framework.redis.core;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

/**
 * A custom implementation of {@link RedisCacheManager} that supports configurable expiration times.
 *
 * When {@link Cacheable#cacheNames()} is in the format "key#ttl",
 * the ttl after the "#" represents the expiration time.
 *
 * The unit is determined by the last character:
 * supported units are d (days), h (hours), m (minutes), s (seconds).
 * The default unit is seconds (s).
 *
 */
public class TimeoutRedisCacheManager extends RedisCacheManager {
    private static final String SPLIT = "#";

    public TimeoutRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        if (StrUtil.isEmpty(name)) {
            return super.createRedisCache(name, cacheConfig);
        }

        // If split by "#", and the length is not 2, it means no custom TTL is used
        String[] names = StrUtil.splitToArray(name, SPLIT);
        if (names.length != 2) {
            return super.createRedisCache(name, cacheConfig);
        }

        // Core logic: modify cacheConfig TTL to support custom expiration time
        if (cacheConfig != null) {
            // Remove ":" and anything after it to avoid parsing issues
            String ttlStr = StrUtil.subBefore(names[1], StrUtil.COLON, false); // extract TTL part
            names[1] = StrUtil.subAfter(names[1], ttlStr, false); // remove TTL part

            // Parse duration
            Duration duration = parseDuration(ttlStr);
            cacheConfig = cacheConfig.entryTtl(duration);
        }

        // Create RedisCache object, ignoring ttlStr part in the final key
        return super.createRedisCache(names[0] + names[1], cacheConfig);
    }

    /**
     * Parse expiration time into Duration
     *
     * @param ttlStr expiration time string
     * @return Duration representing the expiration time
     */
    private Duration parseDuration(String ttlStr) {
        String timeUnit = StrUtil.subSuf(ttlStr, -1);

        switch (timeUnit) {
            case "d":
                return Duration.ofDays(removeDurationSuffix(ttlStr));
            case "h":
                return Duration.ofHours(removeDurationSuffix(ttlStr));
            case "m":
                return Duration.ofMinutes(removeDurationSuffix(ttlStr));
            case "s":
                return Duration.ofSeconds(removeDurationSuffix(ttlStr));
            default:
                return Duration.ofSeconds(Long.parseLong(ttlStr));
        }
    }

    /**
     * Remove the suffix and return the numeric time value.
     *
     * @param ttlStr expiration time string
     * @return numeric time value
     */
    private Long removeDurationSuffix(String ttlStr) {
        return NumberUtil.parseLong(StrUtil.sub(ttlStr, 0, ttlStr.length() - 1));
    }
}
