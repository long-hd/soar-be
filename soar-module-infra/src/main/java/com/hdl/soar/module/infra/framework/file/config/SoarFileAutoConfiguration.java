package com.hdl.soar.module.infra.framework.file.config;

import com.hdl.soar.module.infra.framework.file.core.client.FileClientFactory;
import com.hdl.soar.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * File storage auto-configuration. Registers the {@link FileClientFactory}.
 */
@AutoConfiguration
public class SoarFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
