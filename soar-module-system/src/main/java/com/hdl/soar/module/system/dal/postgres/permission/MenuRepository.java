package com.hdl.soar.module.system.dal.postgres.permission;

import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<MenuPO, Long> {
}
