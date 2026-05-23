package com.hdl.soar.framework.tenant.core.db;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.TenantId;

/**
 * Base persistence object for multi-tenant entities.
 *
 * <p>Entities whose table contains a {@code tenant_id} column should extend this class.
 * Hibernate will automatically:
 * <ul>
 *   <li>Populate {@code tenant_id} on INSERT (from {@link org.hibernate.context.spi.CurrentTenantIdentifierResolver})</li>
 *   <li>Append {@code WHERE tenant_id = ?} on SELECT / UPDATE / DELETE</li>
 * </ul>
 *
 * <p>Entities without tenant isolation (e.g. {@code MenuPO}, {@code OAuth2ClientPO})
 * should extend {@link BasePO} directly.
 *
 * @see org.hibernate.annotations.TenantId
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public abstract class TenantBasePO extends BasePO {

    /**
     * Tenant ID — auto-managed by Hibernate's {@code @TenantId} mechanism.
     *
     * <p>Do NOT set this field manually in application code.
     * It is populated automatically from {@code TenantContextHolder} via the tenant identifier resolver.
     */
    @TenantId
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

}
