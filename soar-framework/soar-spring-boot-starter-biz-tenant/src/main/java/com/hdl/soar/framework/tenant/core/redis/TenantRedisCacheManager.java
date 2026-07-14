package com.hdl.soar.framework.tenant.core.redis;

import com.hdl.soar.framework.redis.core.TimeoutRedisCacheManager;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.util.Set;

/**
 * Tenant-aware {@link org.springframework.data.redis.cache.RedisCacheManager}.
 *
 * <p>For any cache NOT listed in {@code ignoreCaches}, appends ":{tenantId}" to the cache name,
 * giving each tenant an isolated Redis key namespace ({@code <cacheName>:<tenantId>:<key>}).
 * Caches listed in {@code ignoreCaches} are left untouched and shared across tenants (global data).
 *
 * <p>No suffix is added when there is no active tenant context (ignore mode, or tenant id absent),
 * preserving the original behaviour for tenant-ignored flows.
 */
public class TenantRedisCacheManager extends TimeoutRedisCacheManager {

    /** Separator the parent uses to encode a per-cache TTL ("name#ttl"). */
    private static final String TTL_SPLIT = "#";

    private final Set<String> ignoreCaches;

    public TenantRedisCacheManager(RedisCacheWriter cacheWriter,
                                   RedisCacheConfiguration defaultCacheConfiguration,
                                   Set<String> ignoreCaches) {
        super(cacheWriter, defaultCacheConfiguration);
        this.ignoreCaches = ignoreCaches;
    }

    @Override
    public Cache getCache(String name) {
        // Base name = the part before any "#ttl" suffix; used to match the ignore list.
        String baseName = name;
        int hashIndex = name.indexOf(TTL_SPLIT);
        if (hashIndex >= 0) {
            baseName = name.substring(0, hashIndex);
        }

        // Append tenant suffix only when a tenant context is active and the cache is not global.
        // The parent's createRedisCache() strips this ":{tenantId}" back off the TTL token,
        // so appending to the full name is safe even for "name#ttl" caches.
        if (!TenantContextHolder.isIgnore()
                && TenantContextHolder.getTenantId() != null
                && !ignoreCaches.contains(baseName)) {
            name = name + ":" + TenantContextHolder.getTenantId();
        }
        return super.getCache(name);
    }

}
