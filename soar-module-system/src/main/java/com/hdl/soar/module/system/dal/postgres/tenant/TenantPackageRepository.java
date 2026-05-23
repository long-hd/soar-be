package com.hdl.soar.module.system.dal.postgres.tenant;

import com.hdl.soar.module.system.dal.entity.tenant.TenantPackagePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantPackageRepository extends JpaRepository<TenantPackagePO, Long>,
        JpaSpecificationExecutor<TenantPackagePO> {
}
