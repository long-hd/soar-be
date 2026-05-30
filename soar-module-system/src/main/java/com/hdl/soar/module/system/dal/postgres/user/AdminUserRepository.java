package com.hdl.soar.module.system.dal.postgres.user;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUserPO, Long>, JpaSpecificationExecutor<AdminUserPO> {
    Optional<AdminUserPO> findByUsername(String username);

    Optional<AdminUserPO> findByMobile(String mobile);

    Optional<AdminUserPO> findByEmail(String email);

    List<AdminUserPO> findAllByStatus(CommonStatusEnum status);
}
