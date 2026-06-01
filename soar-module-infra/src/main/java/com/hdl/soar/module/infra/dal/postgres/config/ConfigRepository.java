package com.hdl.soar.module.infra.dal.postgres.config;

import com.hdl.soar.module.infra.dal.entity.config.ConfigPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ConfigRepository extends JpaRepository<ConfigPO, Long>,
        JpaSpecificationExecutor<ConfigPO> {

    Optional<ConfigPO> findByConfigKey(String configKey);

}
