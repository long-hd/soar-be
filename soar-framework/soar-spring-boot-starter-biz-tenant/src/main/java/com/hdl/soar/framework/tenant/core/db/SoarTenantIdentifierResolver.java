package com.hdl.soar.framework.tenant.core.db;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;

import java.util.Map;

/**
 * Resolves the current tenant identifier for Hibernate's discriminator-based multi-tenancy.
 *
 * <p>Works with {@link org.hibernate.annotations.TenantId} annotation on entities.
 * Reads tenant from {@link TenantContextHolder}, which is populated by
 * {@code TenantContextWebFilter} early in the filter chain.
 *
 * <p>Implements {@link HibernatePropertiesCustomizer} to self-register
 * as the Hibernate tenant resolver — ensures correct registration regardless
 * of Spring Boot version auto-detection behavior.
 *
 * <p>Returns {@code String} type — Hibernate internally coerces to the
 * {@code @TenantId} field type ({@code Long}) via its {@code JavaType} system.
 *
 * @author hdl
 */
public class SoarTenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<Long>, HibernatePropertiesCustomizer {

    /**
     * Fallback tenant ID used during bootstrap (EntityManagerFactory creation),
     * Flyway migrations, or when no tenant context exists.
     *
     * <p>When combined with {@link #isRoot(String)} returning {@code true},
     * this value is never actually used for filtering — Hibernate skips
     * the tenant predicate entirely.
     */
    private static final Long DEFAULT_TENANT_ID = 0L;

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
    }

    /**
     * Whether to validate that existing sessions match the current tenant.
     *
     * <p>Set to {@code false} because Soar uses transaction-scoped EntityManagers
     * (no open-in-view), so session reuse across different tenants does not occur.
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    /**
     * Determines if the given tenant should bypass tenant filtering (root access).
     *
     * <p>Returns {@code true} when {@link TenantContextHolder#isIgnore()} is set,
     * which happens in these scenarios:
     * <ul>
     *   <li>Method annotated with {@code @TenantIgnore}</li>
     *   <li>{@code TenantUtils.executeIgnore(Runnable)} wrapper</li>
     *   <li>Public URL without tenant header (set by TenantSecurityWebFilter)</li>
     * </ul>
     *
     * <p>When {@code true}, Hibernate omits the {@code WHERE tenant_id = ?}
     * predicate — the query returns data across all tenants.
     */
    @Override
    public boolean isRoot(Long tenantId) {
        return TenantContextHolder.isIgnore();
    }

    /**
     * Self-register as Hibernate's tenant identifier resolver.
     *
     * <p>Belt-and-suspenders: Spring Boot 3.2+ auto-detects
     * {@link CurrentTenantIdentifierResolver} beans, but explicit registration
     * via properties ensures compatibility across versions.
     */
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
