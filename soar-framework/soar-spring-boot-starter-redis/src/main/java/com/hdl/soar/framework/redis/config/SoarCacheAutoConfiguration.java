package com.hdl.soar.framework.redis.config;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.redis.core.TimeoutRedisCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static com.hdl.soar.framework.redis.config.SoarRedisAutoConfiguration.buildRedisSerializer;

/**
 * Cache configuration class, implemented based on Redis.
 */
@AutoConfiguration
@EnableConfigurationProperties({CacheProperties.class,SoarCacheProperties.class})
@EnableCaching
public class SoarCacheAutoConfiguration {

    /**
     * RedisCacheConfiguration Bean
     * <p>
     * Refer to the createConfiguration method in
     * org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration
     */
    @Bean
    @Primary
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // Use a single ":" colon instead of double "::" colons
        // to avoid extra spaces in Redis Desktop Manager.
        // See details: https://blog.csdn.net/chuixue24/article/details/103928965
        // Fix the single ":" vs double "::" colon issue again.
        // Issue details: https://gitee.com/zhijiantianya/yudao-cloud/issues/I86VY2
        config = config.computePrefixWith(cacheName -> {
            String keyPrefix = cacheProperties.getRedis().getKeyPrefix();
            if (StringUtils.hasText(keyPrefix)) {
                keyPrefix = keyPrefix.lastIndexOf(StrUtil.COLON) == -1 ? keyPrefix + StrUtil.COLON : keyPrefix;
                return keyPrefix + cacheName + StrUtil.COLON;
            }
            return cacheName + StrUtil.COLON;
        });
        // Use JSON serialization
        config = config.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(buildRedisSerializer()));

        // Set properties for CacheProperties.Redis
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate<String, Object> redisTemplate,
                                               RedisCacheConfiguration redisCacheConfiguration,
                                               SoarCacheProperties soarCacheProperties) {
        // Create RedisCacheWriter object
        RedisConnectionFactory connectionFactory = Objects.requireNonNull(redisTemplate.getConnectionFactory());

        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(
                connectionFactory,
                BatchStrategies.scan(soarCacheProperties.getRedisScanBatchSize())
        );

        // Create TenantRedisCacheManager object
        return new TimeoutRedisCacheManager(cacheWriter, redisCacheConfiguration);
    }
}
