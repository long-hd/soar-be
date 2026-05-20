package com.hdl.soar.module.system.dal.entity.permission;

import com.hdl.soar.framework.tenant.core.db.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Role-Menu association
 */
@Entity
@Table(name = "system_role_menu")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuEntity extends TenantBaseEntity {

    /**
     * Auto-increment primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Role ID
     */
    @Column(name = "role_id")
    private Long roleId;

    /**
     * Menu ID
     */
    @Column(name = "menu_id")
    private Long menuId;

}
