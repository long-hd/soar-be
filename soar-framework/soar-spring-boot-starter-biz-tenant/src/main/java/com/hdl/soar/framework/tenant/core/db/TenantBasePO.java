package com.hdl.soar.framework.tenant.core.db;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Extended BaseDO for multi-tenancy support
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantBasePO extends BasePO {

    /**
     * Tenant ID
     */
    private Long tenantId;

}
