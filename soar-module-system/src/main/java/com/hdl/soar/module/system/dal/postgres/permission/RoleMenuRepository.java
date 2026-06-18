package com.hdl.soar.module.system.dal.postgres.permission;

import com.hdl.soar.module.system.dal.entity.permission.RoleMenuPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenuPO, Long> {
    List<RoleMenuPO> findAllByMenuId(Long menuId);

    @Query("SELECT rm.menuId FROM RoleMenuPO rm WHERE rm.roleId IN :roleIds")
    Set<Long> findMenuIdsByRoleIdIn(@Param("roleIds") Collection<Long> roleIds);

    @Query("SELECT rm.menuId FROM RoleMenuPO rm WHERE rm.roleId = :roleId")
    Set<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Transactional
    void deleteByRoleIdAndMenuIdIn(Long roleId, Collection<Long> menuIds);

    void deleteByMenuId(Long menuId);

    void deleteByRoleId(Long roleId);

}
