package com.hdl.soar.framework.common.biz.system.permission.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Response DTO for department data permissions.
 */
@Data
public class DeptDataPermissionRespDTO {

    /**
     * Whether the user can view all data.
     */
    private Boolean all;

    /**
     * Whether the user can view their own data.
     */
    private Boolean self;

    /**
     * Set of department IDs that the user can access.
     */
    private Set<Long> deptIds;

    public DeptDataPermissionRespDTO() {
        this.all = false;
        this.self = false;
        this.deptIds = new HashSet<>();
    }

}
