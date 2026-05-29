package com.hdl.soar.module.system.service.tenant;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.util.collection.CollectionUtils;
import com.hdl.soar.framework.common.util.date.InstantUtils;
import com.hdl.soar.framework.tenant.config.TenantProperties;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.module.system.dal.entity.tenant.TenantPO;
import com.hdl.soar.module.system.dal.entity.tenant.TenantPackagePO;
import com.hdl.soar.module.system.dal.postgres.tenant.TenantPackageRepository;
import com.hdl.soar.module.system.dal.postgres.tenant.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TenantServiceImpl implements TenantService {

    TenantProperties tenantProperties;
    TenantRepository tenantRepository;
    TenantPackageRepository tenantPackageRepository;


    @Override
    public TenantPO getTenant(Long id) {
        return tenantRepository.findById(id).orElse(null);
    }

    @Override
    public List<Long> getTenantIdList() {
        List<TenantPO> tenants = tenantRepository.findAll();
        return CollectionUtils.convertList(tenants, TenantPO::getId);
    }

    @Override
    public void validTenant(Long id) {
        TenantPO tenant = getTenant(id);
        if (tenant == null) {
            throw exception(TENANT_NOT_EXISTS);
        }
        if (tenant.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(TENANT_DISABLE, tenant.getName());
        }
        if (InstantUtils.isExpired(tenant.getExpireTime())) {
            throw exception(TENANT_EXPIRE, tenant.getName());
        }
    }

    @Override
    public Set<Long> getTenantMenuIds() {
        // 1. Tenant disabled -> no filtering
        if (!tenantProperties.isEnable()) {
            return null;
        }

        // 2. Get current tenant
        TenantPO tenant = getTenant(TenantContextHolder.getRequiredTenantId());
        if (tenant == null) {
            return null;
        }

        // 3. System tenant -> no filtering (has all menus)
        if (TenantPO.PACKAGE_ID_SYSTEM.equals(tenant.getPackageId())) {
            return null;
        }

        // 4. Get package menu IDs
        TenantPackagePO pkg = tenantPackageRepository.findById(tenant.getPackageId()).orElse(null);
        return pkg != null ? pkg.getMenuIds() : null;
    }

}
