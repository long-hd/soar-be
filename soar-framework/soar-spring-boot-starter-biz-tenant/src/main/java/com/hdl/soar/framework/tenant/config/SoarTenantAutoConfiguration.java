package com.hdl.soar.framework.tenant.config;

import com.hdl.soar.framework.common.biz.system.tenant.TenantCommonApi;
import com.hdl.soar.framework.common.enums.WebFilterOrderEnum;
import com.hdl.soar.framework.tenant.core.aop.TenantIgnore;
import com.hdl.soar.framework.tenant.core.aop.TenantIgnoreAspect;
import com.hdl.soar.framework.tenant.core.db.SoarTenantIdentifierResolver;
import com.hdl.soar.framework.tenant.core.security.TenantSecurityWebFilter;
import com.hdl.soar.framework.tenant.core.service.TenantFrameworkService;
import com.hdl.soar.framework.tenant.core.service.TenantFrameworkServiceImpl;
import com.hdl.soar.framework.tenant.core.web.TenantContextWebFilter;
import com.hdl.soar.framework.web.config.WebProperties;
import com.hdl.soar.framework.web.core.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.convertList;

/**
 * Auto-configuration for Soar's multi-tenant infrastructure.
 *
 * <p>Registers:
 * <ul>
 *   <li>{@link SoarTenantIdentifierResolver} — Hibernate {@code @TenantId} resolver</li>
 *   <li>{@link TenantIgnoreAspect} — AOP for {@code @TenantIgnore} methods</li>
 *   <li>{@link TenantContextWebFilter} — extracts tenant-id header</li>
 *   <li>{@link TenantSecurityWebFilter} — enforces tenant isolation</li>
 * </ul>
 *
 * <p>Disabled entirely with {@code soar.tenant.enable=false}.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "soar.tenant", name = "enable", matchIfMissing = true)
public class SoarTenantAutoConfiguration {

    private final ApplicationContext applicationContext;

    public SoarTenantAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // ========== DB (Hibernate @TenantId) ==========

    @Bean
    public SoarTenantIdentifierResolver tenantIdentifierResolver() {
        return new SoarTenantIdentifierResolver();
    }

    // ========== Service ==========

    @Bean
    public TenantFrameworkService tenantFrameworkService(TenantCommonApi tenantApi) {
        return new TenantFrameworkServiceImpl(tenantApi);
    }

    // ========== AOP ==========

    @Bean
    public TenantIgnoreAspect tenantIgnoreAspect() {
        return new TenantIgnoreAspect();
    }

    // ========== Web ==========

    @Bean
    public FilterRegistrationBean<TenantContextWebFilter> tenantContextWebFilter() {
        FilterRegistrationBean<TenantContextWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantContextWebFilter());
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_CONTEXT_FILTER);
        return registrationBean;
    }

    // TODO [Tenant Visit] TenantVisitContextInterceptor — allows admin with "system:tenant:visit"
    //  permission to temporarily view another tenant's data via visit-tenant-id header.
    //  Requires: TenantVisitContextInterceptor, SecurityFrameworkService,
    //  WebMvcConfigurer to register interceptor, TenantProperties.ignoreVisitUrls

    // ========== Security ==========

    @Bean
    public FilterRegistrationBean<TenantSecurityWebFilter> tenantSecurityWebFilter(
            TenantProperties tenantProperties,
            WebProperties webProperties,
            GlobalExceptionHandler globalExceptionHandler,
            TenantFrameworkService tenantFrameworkService) {
        FilterRegistrationBean<TenantSecurityWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantSecurityWebFilter(
                webProperties, tenantProperties, getTenantIgnoreUrls(),
                globalExceptionHandler, tenantFrameworkService));
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_SECURITY_FILTER);
        return registrationBean;
    }

    /**
     * Scans all controller request mappings at startup.
     * If a handler method or its declaring class is annotated with {@link TenantIgnore},
     * the mapped URL patterns are collected so {@link TenantSecurityWebFilter}
     * allows those requests without a {@code tenant-id} header.
     */
    private Set<String> getTenantIgnoreUrls() {
        Set<String> ignoreUrls = new HashSet<>();
        RequestMappingHandlerMapping handlerMapping = (RequestMappingHandlerMapping)
                applicationContext.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!handlerMethod.hasMethodAnnotation(TenantIgnore.class)
                    && !handlerMethod.getBeanType().isAnnotationPresent(TenantIgnore.class)) {
                continue;
            }
            if (entry.getKey().getPatternsCondition() != null) {
                ignoreUrls.addAll(entry.getKey().getPatternsCondition().getPatterns());
            }
            if (entry.getKey().getPathPatternsCondition() != null) {
                ignoreUrls.addAll(convertList(
                        entry.getKey().getPathPatternsCondition().getPatterns(),
                        PathPattern::getPatternString));
            }
        }
        return ignoreUrls;
    }

    // ========== MQ ==========

    // TODO [Tenant MQ] TenantRedisMessageInterceptor — propagate tenant context through Redis MQ messages
    // TODO [Tenant MQ] TenantRabbitMQInitializer — @ConditionalOnClass(RabbitTemplate)
    // TODO [Tenant MQ] TenantRocketMQInitializer — @ConditionalOnClass(RocketMQTemplate)

    // ========== Job ==========

    // TODO [Tenant Job] TenantJobAspect — iterate all tenants for scheduled jobs.
    //  Requires: TenantFrameworkService.getTenantIds(), TenantUtils.execute(tenantId, runnable)

    // ========== Redis Cache ==========

    // TODO [Tenant Cache] TenantRedisCacheManager — tenant-aware Redis cache with key prefix.
    //  Requires: TenantProperties.ignoreCaches, @Primary RedisCacheManager bean

}
