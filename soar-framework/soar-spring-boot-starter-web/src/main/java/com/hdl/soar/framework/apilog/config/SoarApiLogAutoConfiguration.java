package com.hdl.soar.framework.apilog.config;

import com.hdl.soar.framework.apilog.core.filter.ApiAccessLogFilter;
import com.hdl.soar.framework.apilog.core.interceptor.ApiAccessLogInterceptor;
import com.hdl.soar.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import com.hdl.soar.framework.common.enums.WebFilterOrderEnum;
import com.hdl.soar.framework.web.config.SoarWebAutoConfiguration;
import com.hdl.soar.framework.web.config.WebProperties;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@AutoConfiguration(after = SoarWebAutoConfiguration.class)
public class SoarApiLogAutoConfiguration implements WebMvcConfigurer {

    private final boolean isProd;

    public SoarApiLogAutoConfiguration(Environment environment) {
        this.isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    @Bean
    @ConditionalOnProperty(prefix = "soar.access-log", value = "enable", matchIfMissing = true)
    public FilterRegistrationBean<ApiAccessLogFilter> apiAccessLogFilter(
            WebProperties webProperties,
            @Value("${spring.application.name}") String applicationName,
            ApiAccessLogCommonApi apiAccessLogApi) {
        ApiAccessLogFilter filter = new ApiAccessLogFilter(webProperties, applicationName, apiAccessLogApi);
        return createFilterBean(filter, WebFilterOrderEnum.API_ACCESS_LOG_FILTER);
    }

    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiAccessLogInterceptor(isProd));
    }

}