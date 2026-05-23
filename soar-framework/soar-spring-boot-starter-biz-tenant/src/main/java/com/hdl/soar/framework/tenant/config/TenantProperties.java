package com.hdl.soar.framework.tenant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Set;

/**
 * Multi-tenant configuration properties.
 *
 * <p>Usage in {@code application.yml}:
 * <pre>{@code
 * soar:
 *   tenant:
 *     enable: true
 *     ignore-urls:
 *       - /auth/login
 *       - /auth/register
 *       - /auth/captcha
 * }</pre>
 *
 * @author hdl
 */
@Data
@ConfigurationProperties(prefix = "soar.tenant")
public class TenantProperties {

    /**
     * Whether the tenant is enabled
     */
    private static final Boolean ENABLE_DEFAULT = true;

    /**
     * Whether multi-tenancy is enabled.
     *
     * <p>When {@code false}, the entire tenant infrastructure is disabled:
     * no web filters, no Hibernate resolver, no AOP aspects.
     * Controlled via {@code @ConditionalOnProperty} in auto-configuration.
     */
    private boolean enable = ENABLE_DEFAULT;

    /**
     * URL patterns that do NOT require a {@code tenant-id} request header.
     *
     * <p>Typical entries: login, registration, captcha, webhook callbacks,
     * health checks — any endpoint accessible without tenant context.
     *
     * <p>Supports Ant-style patterns (e.g. {@code /open-api/**}).
     *
     * <p>When a request matches an ignore URL and no tenant header is present,
     * {@code TenantContextHolder.setIgnore(true)} is set automatically,
     * causing Hibernate to skip tenant filtering for that request.
     */
    private Set<String> ignoreUrls = Collections.emptySet();

    /**
     * Requests that need to ignore cross-tenant (tenant switching) access
     *
     * <p>Reason: Some APIs access personal information, which cannot be retrieved across tenants.
     */
    private Set<String> ignoreVisitUrls = Collections.emptySet();


    /**
     * Spring Cache caches that need to ignore multi-tenancy
     *
     * <p>By default, all caches have multi-tenant support enabled,
     * so remember to add the corresponding tenant_id field.
     */
    private Set<String> ignoreCaches = Collections.emptySet();

}
