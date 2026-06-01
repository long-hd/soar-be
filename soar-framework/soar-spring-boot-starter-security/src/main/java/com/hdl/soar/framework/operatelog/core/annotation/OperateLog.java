package com.hdl.soar.framework.operatelog.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Operate log annotation.
 * <p>
 * Place on service methods to automatically record business operation logs.
 * The AOP aspect intercepts annotated methods, resolves SpEL expressions
 * from method parameters and return value, then persists the log asynchronously.
 *
 * <h3>SpEL Context Variables</h3>
 * <ul>
 *   <li>{@code #paramName} — method parameter by name</li>
 *   <li>{@code #result} — method return value (null if void or exception)</li>
 *   <li>{@code #reqDTO}, {@code #id}, etc. — actual parameter names</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * @OperateLog(module = MODULE, name = "Create User",
 *             bizId = "#result",
 *             content = "'Created user [' + #reqDTO.nickname + ']'")
 * public Long createUser(UserSaveReqDTO reqDTO) { ... }
 * }</pre>
 *
 * @author hdl
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {

    /**
     * Operation module (e.g., "System User", "System Role").
     * <p>
     * Typically a constant from {@code OperateLogConstants}.
     */
    String module();

    /**
     * Operation name (e.g., "Create User", "Delete Role").
     */
    String name();

    /**
     * SpEL expression to resolve the business entity ID.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code "#result"} — return value (for create methods returning ID)</li>
     *   <li>{@code "#result.id"} — field of return value</li>
     *   <li>{@code "#id"} — method parameter named "id"</li>
     *   <li>{@code "#reqDTO.id"} — field of a parameter</li>
     * </ul>
     */
    String bizId() default "";

    /**
     * SpEL expression for human-readable action content.
     * <p>
     * When empty, the aspect logs "{module} - {name}" as default content.
     * <p>
     * Example: {@code "'Created user [' + #reqDTO.nickname + ']'"}
     */
    String content() default "";

    /**
     * SpEL expression for extra JSON fields.
     * <p>
     * Useful for recording additional context (order number, etc.).
     */
    String extra() default "";

    /**
     * Whether to enable logging for this method.
     * <p>
     * Set to false to temporarily disable without removing the annotation.
     */
    boolean enable() default true;

}
