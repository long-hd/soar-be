package com.hdl.soar.framework.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Cache configuration properties
 */
@ConfigurationProperties("soar.cache")
@Data
@Validated
public class SoarCacheProperties {
    /**
     * Default value for {@link #redisScanBatchSize}
     */
    private static final Integer REDIS_SCAN_BATCH_SIZE_DEFAULT = 30;

    /**
     * Number of results returned by Redis scan in a single iteration
     */
    private Integer redisScanBatchSize = REDIS_SCAN_BATCH_SIZE_DEFAULT;
}
