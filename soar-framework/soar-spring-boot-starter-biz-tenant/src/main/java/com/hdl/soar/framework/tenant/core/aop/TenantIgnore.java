package com.hdl.soar.framework.tenant.core.aop;

import java.lang.annotation.*;

/**
 * Marks a method or class to bypass tenant filtering.
 *
 * <p>When placed on a <b>service/repository method</b>: the AOP aspect sets
 * {@code TenantContextHolder.setIgnore(true)} for the duration of that method,
 * causing Hibernate's {@code @TenantId} filter to be skipped.
 *
 * <p>When placed on a <b>controller class</b>: the auto-configuration scans
 * all request mappings at startup and adds matching URLs to the tenant
 * ignore list, so {@code TenantSecurityWebFilter} allows requests
 * without a {@code tenant-id} header.
 *
 * <p>Typical use cases:
 * <ul>
 *   <li>Loading all tenants for a dropdown (admin panel)</li>
 *   <li>Cron jobs that process data across tenants</li>
 *   <li>Public endpoints (login, registration, health check)</li>
 * </ul>
 *
 * <p>For programmatic ignore within a block of code, use
 * {@link com.hdl.soar.framework.tenant.core.util.TenantUtils#executeIgnore(Runnable)} instead.
 *
 * @see TenantIgnoreAspect
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TenantIgnore {

    /**
     * SpEL expression that determines whether tenant filtering should be skipped.
     *
     * <p>Supports method parameter references (e.g. {@code #isSuperAdmin}).
     * When the expression evaluates to {@code true}, tenant filtering is bypassed.
     *
     * <p>Defaults to {@code "true"} (always ignore when annotation is present).
     *
     * <p>Example:
     * <pre>{@code
     * @TenantIgnore(enable = "#isSuperAdmin")
     * public List<User> listUsers(boolean isSuperAdmin) { ... }
     * }</pre>
     */
    String enable() default "true";

}
