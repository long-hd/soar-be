package com.hdl.soar.module.system.dal.entity.logger;

import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import com.hdl.soar.module.system.enums.logger.LoginResultEnum;
import com.hdl.soar.module.system.enums.logger.LoginLogTypeEnum;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import lombok.experimental.SuperBuilder;

/**
 * Login Log Table
 * <p>
 * Note: Includes both login and logout actions
 */
@Entity
@Table(name = "system_login_log")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class LoginLogPO extends TenantBasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Log type
     * <p>
     * Enum {@link LoginLogTypeEnum}
     */
    @Column(name = "log_type", nullable = false)
    private LoginLogTypeEnum logType;

    /**
     * Trace ID
     */
    @Column(name = "trace_id")
    private String traceId;

    /**
     * User ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * User type
     * <p>
     * Enum {@link UserTypeEnum}
     */
    @Column(name = "user_type")
    private UserTypeEnum userType;

    /**
     * Username
     * <p>
     * Redundant field, because the account may change
     */
    @Column(name = "username")
    private String username;

    /**
     * Login result
     * <p>
     * Enum {@link LoginResultEnum}
     */
    @Column(name = "result")
    private LoginResultEnum result;

    /**
     * User IP
     */
    @Column(name = "user_ip")
    private String userIp;

    /**
     * Browser User-Agent
     */
    @Column(name = "user_agent")
    private String userAgent;

}
