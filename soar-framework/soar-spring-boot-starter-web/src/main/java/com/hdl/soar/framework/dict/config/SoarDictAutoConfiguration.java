package com.hdl.soar.framework.dict.config;

import com.hdl.soar.framework.common.biz.system.dict.DictDataCommonApi;
import com.hdl.soar.framework.common.biz.system.dict.util.DictFrameworkUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SoarDictAutoConfiguration {

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictFrameworkUtils dictFrameworkUtils(DictDataCommonApi dictDataApi) {
        DictFrameworkUtils.init(dictDataApi);
        return new DictFrameworkUtils();
    }

}
