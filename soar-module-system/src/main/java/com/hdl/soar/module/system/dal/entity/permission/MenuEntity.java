package com.hdl.soar.module.system.dal.entity.permission;

import com.hdl.soar.framework.jpa.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import com.hdl.soar.module.system.enums.permission.MenuTypeEnum;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;

/**
 * Menu Entity
 */
@Entity
@Table(name = "system_menu")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuEntity extends BaseEntity {

    /**
     * Menu ID - Root node
     */
    public static final Long ID_ROOT = 0L;

    /**
     * Role ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Menu name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Permission identifier
     * <p>
     * General format: ${system}:${module}:${action}
     * Example: system:admin:add, meaning adding an admin in the system service.
     * </p>
     * <p>
     * When this MenuDO is assigned to a role, it grants access to this resource:
     * - Backend: works with @PreAuthorize to secure API endpoints.
     * - Frontend: used to control button visibility so users without permission cannot see or use certain actions.
     * </p>
     */
    private String permission;

    /**
     * Menu type
     *<p>
     * Enum {@link MenuTypeEnum}
     */
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * Display order
     */
    @Column(name = "sort", nullable = false)
    @Builder.Default
    private Integer sort = 0;

    /**
     * Parent menu ID
     */
    @Column(name = "parent_id", nullable = false)
    @Builder.Default
    private Long parentId = 0L;

    /**
     * Route path
     * <p>
     * If path starts with http(s), it is treated as an external link.
     */
    @Column(name = "path")
    private String path;

    /**
     * Menu icon
     */
    @Column(name = "icon")
    private String icon;

    /**
     * Component path
     */
    @Column(name = "component")
    private String component;

    /**
     * Component name
     */
    @Column(name = "component_name")
    private String componentName;

    /**
     * Status
     *
     * <p>Enum {@link CommonStatusEnum}
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 0;

    /**
     * Whether visible
     * <p>
     * Used for menu and directory only.
     * When true, the menu is hidden in the sidebar but the route still exists.
     * Example: standalone pages like /edit/1024.
     * </p>
     */
    @Column(name = "visible", nullable = false)
    @Builder.Default
    private Boolean visible = Boolean.TRUE;

    /**
     * Whether cached
     * <p>
     * Used for menu and directory only. Controls Vue keep-alive behavior.
     * Note: if enabled, componentName must be provided for caching to work.</p>
     */
    @Column(name = "keep_alive", nullable = false)
    @Builder.Default
    private Boolean keepAlive = Boolean.TRUE;

    /**
     * Always show
     * <p>
     * If false, and the menu has only one child, the parent menu will not be shown,
     * and the child menu will be displayed directly.</p>
     */
    @Column(name = "always_show", nullable = false)
    @Builder.Default
    private Boolean alwaysShow = Boolean.TRUE;

}
