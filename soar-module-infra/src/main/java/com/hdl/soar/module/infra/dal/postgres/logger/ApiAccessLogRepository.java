package com.hdl.soar.module.infra.dal.postgres.logger;

import com.hdl.soar.module.infra.dal.entity.logger.ApiAccessLogPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApiAccessLogRepository extends JpaRepository<ApiAccessLogPO, Long>,
        JpaSpecificationExecutor<ApiAccessLogPO> {

}
