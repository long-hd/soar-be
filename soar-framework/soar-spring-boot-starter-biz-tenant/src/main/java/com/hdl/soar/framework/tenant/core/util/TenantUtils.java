package com.hdl.soar.framework.tenant.core.util;

import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;

import java.util.Map;
import java.util.concurrent.Callable;

import static com.hdl.soar.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * Multi-tenant Utility
 */
public class TenantUtils {

    /**
     * Execute the specified logic using the given tenant.
     * <p>
     * Note: If tenant ignoring is currently enabled, it will be forcibly disabled.
     * After execution is completed, the original state will be restored.
     *
     * @param tenantId the tenant ID
     * @param runnable the logic to execute
     */
    public static void execute(Long tenantId, Runnable runnable) {
        Long oldTenantId = TenantContextHolder.getTenantId();
        Boolean oldIgnore = TenantContextHolder.isIgnore();

        try {
            TenantContextHolder.setTenantId(tenantId);
            TenantContextHolder.setIgnore(false);

            // Execute logic
            runnable.run();
        } finally {
            TenantContextHolder.setTenantId(oldTenantId);
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * Execute the specified logic using the given tenant.
     * <p>
     * Note: If tenant ignoring is currently enabled, it will be forcibly disabled.
     * After execution is completed, the original state will be restored.
     *
     * @param tenantId the tenant ID
     * @param callable the logic to execute
     * @param <V>      the return type
     * @return the result
     */
    public static <V> V execute(Long tenantId, Callable<V> callable) {
        Long oldTenantId = TenantContextHolder.getTenantId();
        Boolean oldIgnore = TenantContextHolder.isIgnore();

        try {
            TenantContextHolder.setTenantId(tenantId);
            TenantContextHolder.setIgnore(false);

            // Execute logic
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            TenantContextHolder.setTenantId(oldTenantId);
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * Ignore tenant restrictions and execute the specified logic.
     *
     * @param runnable the logic to execute
     */
    public static void executeIgnore(Runnable runnable) {
        Boolean oldIgnore = TenantContextHolder.isIgnore();

        try {
            TenantContextHolder.setIgnore(true);

            // Execute logic
            runnable.run();
        } finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * Ignore tenant restrictions and execute the specified logic.
     *
     * @param callable the logic to execute
     * @param <V>      the return type
     * @return the result
     */
    public static <V> V executeIgnore(Callable<V> callable) {
        Boolean oldIgnore = TenantContextHolder.isIgnore();

        try {
            TenantContextHolder.setIgnore(true);

            // Execute logic
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * Add the tenant ID to the request headers.
     *
     * @param headers  HTTP request headers
     * @param tenantId the tenant ID
     */
    public static void addTenantHeader(Map<String, String> headers, Long tenantId) {
        if (tenantId != null) {
            headers.put(HEADER_TENANT_ID, tenantId.toString());
        }
    }

}
