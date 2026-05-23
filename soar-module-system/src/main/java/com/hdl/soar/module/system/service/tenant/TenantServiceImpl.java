package com.hdl.soar.module.system.service.tenant;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.util.collection.CollectionUtils;
import com.hdl.soar.framework.common.util.date.InstantUtils;
import com.hdl.soar.framework.tenant.config.TenantProperties;
import com.hdl.soar.module.system.dal.entity.tenant.TenantPO;
import com.hdl.soar.module.system.dal.postgres.tenant.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TenantServiceImpl implements TenantService {

    TenantProperties tenantProperties;
    TenantRepository tenantRepository;


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
}
