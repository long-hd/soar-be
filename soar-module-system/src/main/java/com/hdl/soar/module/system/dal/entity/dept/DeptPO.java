package com.hdl.soar.module.system.dal.entity.dept;

import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.*;

import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;

/**
 * Department entity
 */
@Entity
@Table(name = "system_dept")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeptPO extends TenantBasePO {

    public static final Long PARENT_ID_ROOT = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Department name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Parent department ID
     * <p>
     * References {@link #id}
     */
    @Column(name = "parent_id", nullable = false)
    @Builder.Default
    private Long parentId = 0L;

    /**
     * Display order
     */
    @Column(name = "sort", nullable = false)
    @Builder.Default
    private Integer sort = 0;

    /**
     * Department leader
     * <p>
     * References {@link AdminUserPO#getId()}</p>
     */
    @Column(name = "leader_user_id")
    private Long leaderUserId;

    /**
     * Contact phone number
     */
    @Column(name = "phone")
    private String phone;

    /**
     * Email address
     */
    @Column(name = "email")
    private String email;

    /**
     * Department status
     * <p>
     * Enum {@link CommonStatusEnum}</p>
     */
    @Column(name = "status", nullable = false)
    private Integer status;

}
