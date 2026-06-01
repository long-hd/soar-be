package com.hdl.soar.module.infra.dal.postgres.logger;

import com.hdl.soar.module.infra.dal.entity.logger.ApiErrorLogPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiErrorLogRepository extends JpaRepository<ApiErrorLogPO, Long>,
        JpaSpecificationExecutor<ApiErrorLogPO> {

}
