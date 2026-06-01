package com.hdl.soar.framework.operatelog.config;

import com.hdl.soar.framework.common.biz.system.logger.OperateLogCommonApi;
import com.hdl.soar.framework.operatelog.core.aop.OperateLogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author hdl
 */
@AutoConfiguration
public class SoarOperateLogAutoConfiguration {

    @Bean
    public OperateLogAspect operateLogAspect(OperateLogCommonApi operateLogApi) {
        return new OperateLogAspect(operateLogApi);
    }

}
