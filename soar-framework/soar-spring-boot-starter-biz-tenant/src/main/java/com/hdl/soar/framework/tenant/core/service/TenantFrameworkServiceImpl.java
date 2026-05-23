package com.hdl.soar.framework.tenant.core.service;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hdl.soar.framework.common.biz.system.tenant.TenantCommonApi;
import com.hdl.soar.framework.common.exception.ServiceException;
import com.hdl.soar.framework.common.util.cache.CacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.List;

/**
 * Implementation class of the Tenant framework service.
 */
@RequiredArgsConstructor
public class TenantFrameworkServiceImpl implements TenantFrameworkService {

    private static final ServiceException SERVICE_EXCEPTION_NULL = new ServiceException();

    private final TenantCommonApi tenantApi;

    /**
     * Cache for {@link #getTenantIds()}.
     */
    private final LoadingCache<Object, List<Long>> getTenantIdsCache = CacheUtils.buildAsyncReloadingCache(
            Duration.ofMinutes(1L), // Expiration time: 1 minute
            new CacheLoader<Object, List<Long>>() {

                @Override
                public List<Long> load(Object key) {
                    return tenantApi.getTenantIdList();
                }

            });

    /**
     * Cache for {@link #validTenant(Long)}.
     */
    private final LoadingCache<Long, ServiceException> validTenantCache = CacheUtils.buildAsyncReloadingCache(
            Duration.ofMinutes(1L), // Expiration time: 1 minute
            new CacheLoader<Long, ServiceException>() {

                @Override
                public ServiceException load(Long id) {
                    try {
                        tenantApi.validateTenant(id);
                        return SERVICE_EXCEPTION_NULL;
                    } catch (ServiceException ex) {
                        return ex;
                    }
                }

            });

    @Override
    @SneakyThrows
    public List<Long> getTenantIds() {
        return getTenantIdsCache.get(Boolean.TRUE);
    }

    @Override
    public void validTenant(Long id) {
        ServiceException serviceException = validTenantCache.getUnchecked(id);
        if (serviceException != SERVICE_EXCEPTION_NULL) {
            throw serviceException;
        }
    }
}
