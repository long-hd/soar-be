package com.hdl.soar.framework.common.biz.system.tenant;

import java.util.List;

/**
 * Multi-tenant API interface.
 */
public interface TenantCommonApi {

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
    void validateTenant(Long id);

}
