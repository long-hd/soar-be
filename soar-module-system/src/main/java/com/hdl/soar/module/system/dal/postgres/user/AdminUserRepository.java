package com.hdl.soar.module.system.dal.postgres.user;

import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUserPO, Long> {
}
