package com.hdl.soar.module.system.dal.entity.dept;

import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.*;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import lombok.experimental.SuperBuilder;

/**
 * Position entity
 */
@Entity
@Table(name = "system_post")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostPO extends TenantBasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Position name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Position code
     */
    @Column(name = "code", nullable = false)
    private String code;

    /**
     * Position sort order
     */
    @Column(name = "sort", nullable = false)
    private Integer sort;

    /**
     * Status
     * <p>
     * Enum {@link CommonStatusEnum}</p>
     */
    @Column(name = "status", nullable = false)
    private CommonStatusEnum status;

    /**
     * Remark
     */
    @Column(name = "remark")
    private String remark;

}
