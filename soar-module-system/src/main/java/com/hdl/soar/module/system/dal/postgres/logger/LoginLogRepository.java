package com.hdl.soar.module.system.dal.postgres.logger;

import com.hdl.soar.module.system.dal.entity.logger.LoginLogPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLogPO, Long>, JpaSpecificationExecutor<LoginLogPO> {
}
