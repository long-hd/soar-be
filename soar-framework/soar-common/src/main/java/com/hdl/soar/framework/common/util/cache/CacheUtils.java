package com.hdl.soar.framework.common.util.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Cache utility class
 */
public class CacheUtils {

    /**
     * Maximum size of the asynchronous reloading LoadingCache
     *
     * @see <a href="">Recommended usage of CacheUtils local cache utility class</a>
     */
    private static final Integer CACHE_MAX_SIZE = 10000;

    /**
     * Build an asynchronously refreshed LoadingCache instance.
     *
     * <p>Note: If your cache is related to ThreadLocal, you should either handle
     * ThreadLocal propagation yourself, or use {@link #buildCache(Duration, CacheLoader)} instead.
     *
     *  <p>In simple terms: <br>
     * 1. Use {@link #buildCache(Duration, CacheLoader)} for user-related data <br>
     * 2. Use this method for global/system-related data
     *
     * @param duration expiration time
     * @param loader CacheLoader instance
     * @return LoadingCache instance
     */
    public static <K, V> LoadingCache<K, V> buildAsyncReloadingCache(Duration duration, CacheLoader<K, V> loader) {
        return CacheBuilder.newBuilder()
                .maximumSize(CACHE_MAX_SIZE)
                // Only blocks the current loading thread; other threads return stale value
                .refreshAfterWrite(duration)
                // Fully asynchronous loading via asyncReloading, including refreshAfterWrite-triggered loads
                .build(CacheLoader.asyncReloading(loader, Executors.newCachedThreadPool())); // TODO: consider making executor configurable in the future
    }

    /**
     * Build a synchronously refreshed LoadingCache instance.
     *
     * @param duration expiration time
     * @param loader CacheLoader instance
     * @return LoadingCache instance
     */
    public static <K, V> LoadingCache<K, V> buildCache(Duration duration, CacheLoader<K, V> loader) {
        return CacheBuilder.newBuilder()
                .maximumSize(CACHE_MAX_SIZE)
                // Only blocks the current loading thread; other threads return stale value
                .refreshAfterWrite(duration)
                .build(loader);
    }

}
