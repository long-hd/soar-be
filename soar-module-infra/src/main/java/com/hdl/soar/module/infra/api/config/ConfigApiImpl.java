package com.hdl.soar.module.infra.api.config;

import com.hdl.soar.framework.common.biz.infra.config.ConfigCommonApi;
import com.hdl.soar.module.infra.dal.entity.config.ConfigPO;
import com.hdl.soar.module.infra.service.config.ConfigService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigApiImpl implements ConfigCommonApi {

    ConfigService configService;

    @Override
    public String getConfigValueByKey(String key) {
        ConfigPO config = configService.getConfigByKey(key);
        return config != null ? config.getValue() : null;
    }

}