package com.hdl.soar.module.system.dal.postgres.permission;

import com.hdl.soar.module.system.dal.entity.permission.RoleMenuPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenuPO, Long> {
    List<RoleMenuPO> findAllByMenuId(Long menuId);

    Set<Long> findAllByRoleIdIn(Collection<Long> roleIds);
}
