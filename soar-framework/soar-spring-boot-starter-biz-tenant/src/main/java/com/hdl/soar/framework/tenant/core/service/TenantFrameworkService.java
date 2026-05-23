package com.hdl.soar.framework.tenant.core.service;

import java.util.List;

/**
 * Tenant framework service interface, defines methods for retrieving tenant information.
 */
public interface TenantFrameworkService {

    /**
     * Get all tenants.
     *
     * @return list of tenant IDs
     */
    List<Long> getTenantIds();

    /**
     * Validate whether the tenant is valid.
     *
     * @param id tenant ID
     */
    void validTenant(Long id);

}
