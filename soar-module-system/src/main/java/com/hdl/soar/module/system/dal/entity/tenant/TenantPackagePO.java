package com.hdl.soar.module.system.dal.entity.tenant;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.jpa.core.converter.JsonLongSetConverter;
import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Tenant package PO
 */
@Entity
@Table(name = "system_tenant_package")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class TenantPackagePO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Package name, must be unique
     */
    @Column(name = "name")
    private String name;

    /**
     * Tenant package status
     *
     * <p>Enum {@link CommonStatusEnum}
     */
    @Column(name = "status")
    @Builder.Default
    private CommonStatusEnum status = CommonStatusEnum.ENABLE;

    /**
     * Remark
     */
    @Column(name = "remark")
    private String remark;

    /**
     * Associated menu IDs
     */
    @Column(name = "menu_ids")
    @Convert(converter = JsonLongSetConverter.class)
    private Set<Long> menuIds;

}
