package com.hdl.soar.framework.tenant.core.redis;


import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies the cache-name namespacing logic - the whole isolation guarantee lives here.
 * No live Redis needed: getCache() only mangles the name; the RedisCacheWriter is a mock
 * (it isn't touched until an actual get/put happens).
 */
public class TenantRedisCacheManagerTest {

    private static final String TENANT_CACHE = "menu_role_ids";   // tenant-scoped
    private static final String GLOBAL_CACHE = "permission_menu_ids"; // in ignore list

    private final TenantRedisCacheManager manager = new TenantRedisCacheManager(
            mock(RedisCacheWriter.class),
            RedisCacheConfiguration.defaultCacheConfig(),
            Set.of(GLOBAL_CACHE));

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("tenant-scoped cache name gets the tenant id appended")
    void tenantScopedCache_isNamespacedPerTenant() {
        TenantContextHolder.setTenantId(1L);
        assertThat(manager.getCache(TENANT_CACHE).getName()).isEqualTo("menu_role_ids:1");

        // Different tenant -> different cache namespace -> cannot collide.
        TenantContextHolder.setTenantId(2L);
        assertThat(manager.getCache(TENANT_CACHE).getName()).isEqualTo("menu_role_ids:2");
    }

    @Test
    @DisplayName("global cache in ignore-list is never namespaced")
    void globalCache_isNotNamespaced() {
        TenantContextHolder.setTenantId(1L);
        assertThat(manager.getCache(GLOBAL_CACHE).getName()).isEqualTo("permission_menu_ids");
    }

    @Test
    @DisplayName("no suffix when there is no tenant context")
    void noTenantContext_noSuffix() {
        // tenant id not set
        assertThat(manager.getCache(TENANT_CACHE).getName()).isEqualTo("menu_role_ids");
    }

    @Test
    @DisplayName("no suffix in tenant-ignore mode")
    void ignoreMode_noSuffix() {
        TenantContextHolder.setTenantId(1L);
        TenantContextHolder.setIgnore(true);
        assertThat(manager.getCache(TENANT_CACHE).getName()).isEqualTo("menu_role_ids");
    }

}
