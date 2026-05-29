package com.hdl.soar.module.system.dal.postgres.permission;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RolePO, Long>, JpaSpecificationExecutor<RolePO> {
    List<RolePO> findAllByIdIn(Collection<Long> ids);

    Optional<RolePO> findByName(String name);

    Optional<RolePO> findByCode(String code);

    List<RolePO> findAllByStatus(CommonStatusEnum status);
}
