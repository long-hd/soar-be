package com.hdl.soar.module.infra.dal.entity.config;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import com.hdl.soar.module.infra.enums.config.ConfigTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * System configuration entity.
 * <p>
 * Extends {@link BasePO} (not TenantBasePO) — config is global, not per-tenant.
 */
@Entity
@Table(name = "infra_config")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class ConfigPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Config category (e.g., "biz", "system").
     */
    @Column(name = "category", nullable = false)
    private String category;

    /**
     * Config type: SYSTEM (built-in) or CUSTOM (user-created).
     */
    @Column(name = "type", nullable = false)
    private ConfigTypeEnum type;

    /**
     * Human-readable config name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Config key (unique among active records).
     * <p>
     * Named {@code configKey} instead of {@code key} because "key" is
     * a reserved word in some databases.
     */
    @Column(name = "config_key", nullable = false)
    private String configKey;

    /**
     * Config value.
     */
    @Column(name = "value")
    private String value;

    /**
     * Whether visible to frontend.
     * <p>
     * Sensitive configs (e.g., passwords) should be {@code false}.
     */
    @Column(name = "visible", nullable = false)
    private Boolean visible;

    /**
     * Remark.
     */
    @Column(name = "remark")
    private String remark;

}