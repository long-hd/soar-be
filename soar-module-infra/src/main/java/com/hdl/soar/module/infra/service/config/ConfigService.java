package com.hdl.soar.module.infra.service.config;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.infra.controller.admin.config.dto.ConfigPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.config.dto.ConfigSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.config.ConfigPO;
import jakarta.validation.Valid;

public interface ConfigService {

    Long createConfig(@Valid ConfigSaveReqDTO createReqDTO);

    void updateConfig(@Valid ConfigSaveReqDTO updateReqDTO);

    void deleteConfig(Long id);

    ConfigPO getConfig(Long id);

    ConfigPO getConfigByKey(String key);

    PageResult<ConfigPO> getConfigPage(ConfigPageReqDTO pageReqDTO);

}
