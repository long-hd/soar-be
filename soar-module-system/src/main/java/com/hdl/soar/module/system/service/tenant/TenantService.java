package com.hdl.soar.module.system.service.tenant;

import com.hdl.soar.module.system.dal.entity.tenant.TenantPO;

import java.util.List;
import java.util.Set;

/**
 * Tenant service interface.
 */
public interface TenantService {

    /**
     * Get tenant
     *
     * @param id ID
     * @return tenant
     */
    TenantPO getTenant(Long id);

    /**
     * Get all tenants.
     *
     * @return list of tenant IDs
     */
    List<Long> getTenantIdList();

    /**
     * Validate whether the tenant is valid.
     *
     * @param id tenant ID
     */
    void validTenant(Long id);

    /**
     * Get menu IDs allowed for the current tenant's package.
     *
     * @return menu IDs, or null if no filtering needed (system tenant or tenant disabled)
     */
    Set<Long> getTenantMenuIds();

    /**
     * Retrieves the tenant associated with the specified name.
     *
     * @param name the tenant name
     * @return the corresponding tenant
     */
    TenantPO getTenantByName(String name);

    /**
     * Retrieves the tenant associated with the specified website domain.
     *
     * @param website the website domain
     * @return the corresponding tenant
     */
    TenantPO getTenantByWebsite(String website);
}
