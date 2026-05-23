package com.hdl.soar.module.system.dal.postgres.tenant;

import com.hdl.soar.module.system.dal.entity.tenant.TenantPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<TenantPO, Long>, JpaSpecificationExecutor<TenantPO> {
}
