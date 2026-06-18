package com.hdl.soar.module.system.dal.entity.permission;

import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import lombok.experimental.SuperBuilder;

/**
 * User-Role association
 */
@Entity
@Table(name = "system_user_role")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class UserRolePO extends TenantBasePO {

    /**
     * Auto-increment primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Role ID
     */
    @Column(name = "role_id")
    private Long roleId;

}
