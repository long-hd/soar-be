package com.hdl.soar.module.system.enums.permission;

import com.hdl.soar.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Role identifier enumeration
 */
@Getter
@AllArgsConstructor
public enum RoleCodeEnum {

    SUPER_ADMIN("super_admin", "Super Administrator"),
    TENANT_ADMIN("tenant_admin", "Tenant Administrator"),
    CRM_ADMIN("crm_admin", "CRM Administrator"); // Dedicated for CRM system

    /**
     * Role code
     */
    private final String code;

    /**
     * Name
     */
    private final String name;

    public static boolean isSuperAdmin(String code) {
        return ObjectUtils.equalsAny(code, SUPER_ADMIN.getCode());
    }

}
