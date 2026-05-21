package com.hdl.soar.module.system.dal.postgres.permission;

import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RolePO, Long> {
}
