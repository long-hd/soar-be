package com.hdl.soar.module.system.dal.entity.permission;

import com.hdl.soar.framework.jpa.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * User-Role association
 */
@Entity
@Table(name = "system_user_role")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleEntity extends BaseEntity {

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
