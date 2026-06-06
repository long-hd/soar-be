package com.hdl.soar.module.system.dal.postgres.tenant;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.dal.entity.tenant.TenantPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<TenantPO, Long>, JpaSpecificationExecutor<TenantPO> {
    Optional<TenantPO> findByName(String name);

    @Query(value = """
        SELECT *
        FROM system_tenant
        WHERE deleted = false
          AND websites IS NOT NULL
          AND jsonb_exists(CAST(websites AS jsonb), :website)
        LIMIT 1
    """, nativeQuery = true)
    Optional<TenantPO> findByWebsiteContains(@Param("website") String website);
}
