package com.hdl.soar.framework.web.config;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.hdl.soar.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import com.hdl.soar.framework.common.enums.WebFilterOrderEnum;
import com.hdl.soar.framework.web.core.filter.CacheRequestBodyFilter;
import com.hdl.soar.framework.web.core.filter.DemoFilter;
import com.hdl.soar.framework.web.core.handler.GlobalExceptionHandler;
import com.hdl.soar.framework.web.core.handler.GlobalResponseBodyHandler;
import com.hdl.soar.framework.web.core.util.WebFrameworkUtils;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.function.Predicate;

@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
public class SoarWebAutoConfiguration {

    /**
     * Application name
     */
    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public WebMvcRegistrations webMvcRegistrations(WebProperties webProperties) {
        return new WebMvcRegistrations() {

            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
                // Include the prefix when instantiating
                mapping.setPathPrefixes(buildPathPrefixes(webProperties));
                return mapping;
            }

            /**
             * Build a mapping from prefix → matching condition
             */
            private Map<String, Predicate<Class<?>>> buildPathPrefixes(WebProperties webProperties) {
                AntPathMatcher antPathMatcher = new AntPathMatcher(".");
                Map<String, Predicate<Class<?>>> pathPrefixes = Maps.newLinkedHashMapWithExpectedSize(2);
                putPathPrefix(pathPrefixes, webProperties.getAdminApi(), antPathMatcher);
                putPathPrefix(pathPrefixes, webProperties.getAppApi(), antPathMatcher);
                return pathPrefixes;
            }

            /**
             * Set the API prefix, only matching those under the controller package
             */
            private void putPathPrefix(Map<String, Predicate<Class<?>>> pathPrefixes, WebProperties.Api api, AntPathMatcher matcher) {
                if (api == null || StrUtil.isEmpty(api.getPrefix())) {
                    return;
                }
                pathPrefixes.put(api.getPrefix(), // API prefix
                        clazz -> clazz.isAnnotationPresent(RestController.class)
                                && matcher.match(api.getController(), clazz.getPackage().getName()));
            }
        };
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler(ApiErrorLogCommonApi apiErrorLogCommonApi) {
        return new GlobalExceptionHandler(applicationName, apiErrorLogCommonApi);
    }

    @Bean
    public GlobalResponseBodyHandler globalResponseBodyHandler() {
        return new GlobalResponseBodyHandler();
    }

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public WebFrameworkUtils webFrameworkUtils(WebProperties webProperties) {
        // Since WebFrameworkUtils needs to use the webProperties attribute, register it as a Bean
        return new WebFrameworkUtils(webProperties);
    }

    // ========== Filter ==========

    /**
     * Create a CorsFilter Bean to resolve cross-origin issues
     */
    @Bean
    @Order(value = WebFilterOrderEnum.CORS_FILTER) // Special: fix the issue where CORS configuration does not take effect due to execution order
    public FilterRegistrationBean<CorsFilter> corsFilterBean() {
        // Create a CorsConfiguration object
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Set allowed origin
        config.addAllowedHeader("*"); // Set allowed request headers
        config.addAllowedMethod("*"); // Set allowed request methods
        // Create a UrlBasedCorsConfigurationSource object
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply CORS configuration to all endpoints
        return createFilterBean(new CorsFilter(source), WebFilterOrderEnum.CORS_FILTER);
    }

    /**
     * Creates a RequestBodyCacheFilter bean to allow repeated reading of the request body.
     */
    @Bean
    public FilterRegistrationBean<CacheRequestBodyFilter> requestBodyCacheFilter() {
        return createFilterBean(new CacheRequestBodyFilter(), WebFilterOrderEnum.REQUEST_BODY_CACHE_FILTER);
    }

    /**
     * 创建 DemoFilter Bean，演示模式
     */
    @Bean
    @ConditionalOnProperty(value = "soar.demo", havingValue = "true")
    public FilterRegistrationBean<DemoFilter> demoFilter() {
        return createFilterBean(new DemoFilter(), WebFilterOrderEnum.DEMO_FILTER);
    }

    public static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

    /**
     * Creates a default {@link RestClient} bean for synchronous HTTP calls.
     *
     * <p>Replaces the legacy {@link org.springframework.web.client.RestTemplate}
     * which is in maintenance mode since Spring Framework 6.1.
     *
     * <p>The {@link RestClient.Builder} is auto-configured by Spring Boot,
     * pre-loaded with {@link org.springframework.http.converter.HttpMessageConverter}s
     * and {@link org.springframework.boot.http.client.ClientHttpRequestFactorySettings}.
     *
     * <p>Note: IDE may report "No beans of 'RestClient.Builder' type found" in library modules.
     * This is a false positive — the builder is provided at runtime by
     * {@link org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration}.
     *
     * @param builder the auto-configured RestClient builder
     * @return a RestClient instance with default settings
     */
     @Bean
     @ConditionalOnMissingBean
     public RestClient restClient(RestClient.Builder builder) {
         return builder.build();
     }
}
