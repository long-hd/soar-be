package com.hdl.soar.module.infra.dal.entity.logger;

import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import com.hdl.soar.module.infra.enums.logger.ApiErrorLogProcessStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "infra_api_error_log")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class ApiErrorLogPO extends TenantBasePO {

    /**
     * Maximum length of {@link #requestParams}.
     */
    public static final Integer REQUEST_PARAMS_MAX_LENGTH = 8000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Trace identifier for request tracking.
     *
     * <p>In general, the trace ID can be used to correlate access logs, error logs,
     * distributed tracing logs, and application logger outputs, making it easier
     * to diagnose and troubleshoot issues across the entire request lifecycle.
     */
    @Column(name = "trace_id", nullable = false)
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
     * <p>Currently resolved from the {@code spring.application.name} configuration property.
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
    @Column(name = "request_params", columnDefinition = "text", nullable = false)
    private String requestParams;

    @Column(name = "user_ip", nullable = false)
    private String userIp;

    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    // ========== Exception Fields ==========

    /**
     * Timestamp when the exception occurred.
     */
    @Column(name = "exception_time", nullable = false)
    private Instant exceptionTime;

    /**
     * Exception class name.
     *
     * <p>The fully qualified name of the exception class returned by
     * {@link Throwable#getClass()}.
     */
    @Column(name = "exception_name", nullable = false)
    private String exceptionName;

    /**
     * Message describing the exception.
     *
     * <p>Resolved using
     * {@link org.apache.commons.lang3.exception.ExceptionUtils#getMessage(Throwable)}.
     */
    @Column(name = "exception_message", columnDefinition = "text", nullable = false)
    private String exceptionMessage;

    /**
     * Root cause message of the exception.
     *
     * <p>The message of the deepest underlying cause, resolved using
     * {@link cn.hutool.core.exceptions.ExceptionUtil#getRootCauseMessage(Throwable)}.
     */
    @Column(name = "exception_root_cause_message", columnDefinition = "text", nullable = false)
    private String exceptionRootCauseMessage;

    /**
     * Full stack trace of the exception.
     *
     * <p>Generated using
     * {@link org.apache.commons.lang3.exception.ExceptionUtils#getStackTrace(Throwable)}.
     */
    @Column(name = "exception_stack_trace", columnDefinition = "text", nullable = false)
    private String exceptionStackTrace;

    /**
     * Fully qualified class name where the exception occurred.
     *
     * <p>See {@link StackTraceElement#getClassName()}.
     */
    @Column(name = "exception_class_name", nullable = false)
    private String exceptionClassName;

    /**
     * Source file name where the exception occurred.
     *
     * <p>See {@link StackTraceElement#getFileName()}.
     */
    @Column(name = "exception_file_name", nullable = false)
    private String exceptionFileName;

    /**
     * Method name where the exception occurred.
     *
     * <p>See {@link StackTraceElement#getMethodName()}.
     */
    @Column(name = "exception_method_name", nullable = false)
    private String exceptionMethodName;

    /**
     * Line number in the source file where the exception occurred.
     *
     * <p>See {@link StackTraceElement#getLineNumber()}.
     */
    @Column(name = "exception_line_number", nullable = false)
    private Integer exceptionLineNumber;

    // ========== Processing-related fields ==========

    /**
     * Processing status.
     *
     * <p>See {@link ApiErrorLogProcessStatusEnum}.
     */
    @Column(name = "process_status", nullable = false)
    private ApiErrorLogProcessStatusEnum processStatus;

    /**
     * Time when the record was processed.
     */
    @Column(name = "process_time")
    private Instant processTime;

    /**
     * ID of the user who processed this record.
     *
     * <p>References
     * {@code AdminUserDO#getId()}.
     */
    @Column(name = "process_user_id")
    private Long processUserId;

}
