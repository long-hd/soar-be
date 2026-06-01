package com.hdl.soar.module.infra.dal.entity.logger;

import com.hdl.soar.framework.common.enums.OperateTypeEnum;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "infra_api_access_log")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApiAccessLogPO extends TenantBasePO {

    /**
     * Max length for {@link #requestParams} before truncation.
     */
    public static final int REQUEST_PARAMS_MAX_LENGTH = 8000;

    /**
     * Max length for {@link #resultMsg} before truncation.
     */
    public static final int RESULT_MSG_MAX_LENGTH = 512;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Trace ID for request tracking.
     *
     * <p>In general, the trace ID can be used to correlate access logs, error logs,
     * distributed tracing logs, and application logger outputs, enabling efficient
     * troubleshooting and root-cause analysis across the entire request lifecycle.
     */
    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "user_id")
    private Long userId;

    /**
     * Type of the user.
     *
     * <p>Defined by the {@link UserTypeEnum} enumeration.
     */
    @Column(name = "user_type")
    private UserTypeEnum userType;

    /**
     * Application name.
     *
     * <p>Retrieved from the {@code spring.application.name} configuration.
     */
    @Column(name = "application_name", nullable = false)
    private String applicationName;

    // ========== Request Fields ==========

    @Column(name = "request_method", nullable = false)
    private String requestMethod;

    @Column(name = "request_url", nullable = false)
    private String requestUrl;

    /**
     * Request parameters.
     *
     * <ul>
     *   <li>{@code query}: Query string parameters</li>
     *   <li>{@code body}: Request body</li>
     * </ul>
     */
    @Column(name = "request_params", columnDefinition = "text")
    private String requestParams;

    @Column(name = "response_body", columnDefinition = "text")
    private String responseBody;

    @Column(name = "user_ip", nullable = false)
    private String userIp;

    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    // ========== Execution Fields ==========

    /**
     * Operation module.
     */
    @Column(name = "operate_module")
    private String operateModule;

    /**
     * Operation name.
     */
    @Column(name = "operate_name")
    private String operateName;

    /**
     * Operation category.
     *
     * <p>Defined by the {@link OperateTypeEnum} enumeration.
     */
    @Column(name = "operate_type")
    private OperateTypeEnum operateType;

    @Column(name = "begin_time", nullable = false)
    private Instant beginTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    /**
     * Execution duration in milliseconds.
     */
    @Column(name = "duration", nullable = false)
    private Integer duration;

    /**
     * Result code.
     *
     * <p>Currently uses the value returned by {@link CommonResult#getCode()}.
     */
    @Column(name = "result_code", nullable = false)
    private Integer resultCode;

    /**
     * Result message.
     *
     * <p>Currently uses the value returned by {@link CommonResult#getMsg()}.
     */
    @Column(name = "result_msg")
    private String resultMsg;

}
