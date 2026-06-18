package com.hdl.soar.module.system.dal.entity.logger;

import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "system_operate_log")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class OperateLogPO extends TenantBasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Trace ID for correlating with access/error logs.
     */
    @Column(name = "trace_id")
    private String traceId;

    /**
     * User ID who performed the operation.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * User type (admin, member, etc).
     *
     * <p>See {@link UserTypeEnum}.
     */
    @Column(name = "user_type", nullable = false)
    private UserTypeEnum userType;

    /**
     * Operation module (e.g., "System User", "System Role").
     */
    @Column(name = "module", nullable = false)
    private String module;

    /**
     * Operation name (e.g., "Create User", "Update Role").
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Business entity ID (e.g., user ID, role ID).
     */
    @Column(name = "biz_id", nullable = false)
    private Long bizId;

    /**
     * Human-readable action content.
     * <p>
     * Example: "Created user [Long]"
     */
    @Column(name = "content")
    private String content;

    /**
     * Extra fields in JSON format.
     */
    @Column(name = "extra")
    private String extra;

    @Column(name = "request_method")
    private String requestMethod;

    @Column(name = "request_url")
    private String requestUrl;

    @Column(name = "user_ip")
    private String userIp;

    @Column(name = "user_agent")
    private String userAgent;

}
