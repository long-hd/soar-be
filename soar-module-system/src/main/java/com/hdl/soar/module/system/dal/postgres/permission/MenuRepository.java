package com.hdl.soar.module.system.dal.postgres.permission;

import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<MenuPO, Long>, JpaSpecificationExecutor<MenuPO> {
    List<MenuPO> findAllByPermission(String permission);

    List<MenuPO> findAllByIdIn(Collection<Long> ids);

    Optional<MenuPO> findByParentIdAndName(Long parentId, String name);

    Optional<MenuPO> findByComponentName(String componentName);

    double countByParentId(Long parentId);
}
