package com.hdl.soar.module.pay.framework.pay.config;

import com.hdl.soar.module.pay.framework.pay.core.client.PayClientFactory;
import com.hdl.soar.module.pay.framework.pay.core.client.impl.PayClientFactoryImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the pay framework: the client factory bean and the {@link PayProperties} binding.
 */
@Configuration
@EnableConfigurationProperties(PayProperties.class)
public class PayConfiguration {

    @Bean
    public PayClientFactory payClientFactory(PayProperties payProperties) {
        return new PayClientFactoryImpl(payProperties);
    }

}
