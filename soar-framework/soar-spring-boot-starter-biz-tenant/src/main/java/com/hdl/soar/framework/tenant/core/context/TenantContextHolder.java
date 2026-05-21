package com.hdl.soar.framework.tenant.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.hdl.soar.framework.common.enums.DocumentEnum;

/**
 * Multi-tenant context holder
 */
public class TenantContextHolder {

    /**
     * Current tenant ID
     */
    private static final ThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    /**
     * Whether to ignore tenant filtering
     */
    private static final ThreadLocal<Boolean> IGNORE = new TransmittableThreadLocal<>();

    /**
     * Get the tenant ID
     *
     * @return Tenant ID
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * Get the tenant ID. Throws NullPointerException if not present.
     *
     * @return Tenant ID
     */
    public static Long getRequiredTenantId() {
        Long tenantId = getTenantId();
        if (tenantId == null) {
            throw new NullPointerException("TenantContextHolder does not contain a tenant ID! See documentation: "
                    + DocumentEnum.TENANT.getUrl());
        }
        return tenantId;
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void setIgnore(Boolean ignore) {
        IGNORE.set(ignore);
    }

    /**
     * Whether tenant filtering is currently ignored
     *
     * @return true if ignored, false otherwise
     */
    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE.get());
    }

    /**
     * Clear tenant context
     */
    public static void clear() {
        TENANT_ID.remove();
        IGNORE.remove();
    }

}
