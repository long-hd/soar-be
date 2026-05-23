package com.hdl.soar.module.system.api.tenant;

import com.hdl.soar.framework.common.biz.system.tenant.TenantCommonApi;
import com.hdl.soar.module.system.service.tenant.TenantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation class of the multi-tenant API.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TenantApiImpl implements TenantCommonApi {

    TenantService tenantService;

    @Override
    public List<Long> getTenantIdList() {
        return tenantService.getTenantIdList();
    }

    @Override
    public void validateTenant(Long id) {
        tenantService.validTenant(id);
    }
}
