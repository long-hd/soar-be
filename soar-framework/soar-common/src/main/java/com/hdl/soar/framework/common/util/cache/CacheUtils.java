package com.hdl.soar.framework.common.util.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.time.Duration;

/**
 * Cache utility class
 */
public class CacheUtils {

    /**
     * Maximum size of the asynchronous reloading LoadingCache
     */
    private static final Integer CACHE_MAX_SIZE = 10000;

    /**
     * Build an asynchronously-refreshed Caffeine {@link LoadingCache}.
     *
     * <p>Caffeine's {@code refreshAfterWrite} is asynchronous by default: when an entry is older than
     * {@code duration}, the next access returns the stale value and triggers a background reload.
     * No explicit executor or async wrapper is required.
     *
     * <p>Use for global/system data (not user- or ThreadLocal-bound), e.g. the file client cache.
     *
     * @param duration refresh-after-write interval
     * @param loader   value loader
     * @param <K>      key type
     * @param <V>      value type
     * @return a Caffeine LoadingCache
     */
    public static <K, V> LoadingCache<K, V> buildAsyncReloadingCaffeine(
            Duration duration, CacheLoader<K, V> loader) {
        return Caffeine.newBuilder()
                .maximumSize(CACHE_MAX_SIZE)
                .refreshAfterWrite(duration)
                .build(loader);
    }

}