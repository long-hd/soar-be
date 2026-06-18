package com.hdl.soar.module.system.dal.entity.tenant;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.jpa.core.converter.JsonStringListConverter;
import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

/**
 * Tenant
 */
@Entity
@Table(name = "system_tenant")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class TenantPO extends BasePO {

    /**
     * Package ID - System
     */
    public static final Long PACKAGE_ID_SYSTEM = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tenant name, must be unique
     */
    @Column(name = "name")
    private String name;

    /**
     * Contact user ID
     *
     * <p>Reference {@link AdminUserPO#getId()}
     */
    @Column(name = "contact_user_id")
    private Long contactUserId;

    /**
     * Contact name
     */
    @Column(name = "contact_name")
    private String contactName;

    /**
     * Contact mobile number
     */
    @Column(name = "contact_mobile")
    private String contactMobile;

    /**
     * Tenant status
     *
     * <p>Enum {@link CommonStatusEnum}
     */
    @Column(name = "status")
    private CommonStatusEnum status;

    /**
     * Bound domain list
     * <p>
     * 1. For WeChat Mini Program compatibility, appid is also allowed <br>
     * 2. Stored as an array because different systems (admin, frontend) may use different domains
     */
    @Column(name = "websites")
    @Convert(converter = JsonStringListConverter.class)
    private List<String> websites;

    /**
     * Tenant package ID
     *
     * <p>Reference {@link TenantPackagePO#getId()}
     * Special case: system built-in tenant does not use a package, marked by {@link #PACKAGE_ID_SYSTEM}
     */
    @Column(name = "package_id")
    private Long packageId;


    /**
     * Expiration time
     */
    @Column(name = "expire_time")
    private Instant expireTime;

    /**
     * Number of accounts
     */
    @Column(name = "account_count")
    private Integer accountCount;

}
