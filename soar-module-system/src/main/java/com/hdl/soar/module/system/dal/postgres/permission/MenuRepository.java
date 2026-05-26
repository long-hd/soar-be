package com.hdl.soar.module.system.dal.postgres.permission;

import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<MenuPO, Long> {
    List<MenuPO> findAllByPermission(String permission);

    List<MenuPO> findAllByIdIn(Collection<Long> ids);
}
