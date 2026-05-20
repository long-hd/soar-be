package com.hdl.soar.module.system.dal.entity.permission;

import com.hdl.soar.framework.jpa.core.converter.JsonLongSetConverter;
import com.hdl.soar.module.system.enums.permission.DataScopeEnum;
import com.hdl.soar.module.system.enums.permission.RoleTypeEnum;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.tenant.core.db.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * Role DO
 */
@Entity
@Table(name = "system_role")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity extends TenantBaseEntity {

    /**
     * Role ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Role name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Role identifier
     *
     * <p>Enum
     */
    @Column(name = "code", nullable = false)
    private String code;

    /**
     * Role sort order
     */
    @Column(name = "sort", nullable = false)
    private Integer sort;

    /**
     * Role status
     *
     * <p>Enum {@link CommonStatusEnum}
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * Role type
     *
     * <p>Enum {@link RoleTypeEnum}
     */
    @Column(name = "type", nullable = false)
    @Builder.Default
    private Integer type = 1;

    /**
     * Remark
     */
    @Column(name = "remark")
    private String remark;

    /**
     * Data scope
     *
     * <p>Enum {@link DataScopeEnum}
     */
    @Column(name = "data_scope")
    private Integer dataScope;

    /**
     * Data scope (specific department IDs)
     *
     * <p>Applicable when {@link #dataScope} is {@link DataScopeEnum#DEPT_CUSTOM}
     */
    @Convert(converter = JsonLongSetConverter.class)
    @Column(name = "data_scope_dept_ids")
    private Set<Long> dataScopeDeptIds;

}
