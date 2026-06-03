package com.hdl.soar.framework.datapermission.core.annotation;

import com.hdl.soar.framework.datapermission.core.rule.DataPermissionRule;

import java.lang.annotation.*;

/**
 * Data permission annotation.
 *
 * <p>Declared on a class or method to control which data-permission rules apply.
 * Data permission is <b>enabled by default</b> even without this annotation; use
 * {@code @DataPermission(enable = false)} to disable it for a class/method.
 *
 * @see com.hdl.soar.framework.datapermission.core.util.DataPermissionUtils#executeIgnore(Runnable)
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * Whether data permission is enabled for the annotated class/method.
     *
     * <p>Defaults to {@code true}. Set to {@code false} to bypass filtering
     * (e.g. login-by-username lookups, or queries that compute the scope itself
     * and must not recurse).
     */
    boolean enable() default true;

    /**
     * Rules to apply. Takes precedence over {@link #excludeRules()}.
     * When empty, all registered rules apply (subject to {@link #excludeRules()}).
     */
    Class<? extends DataPermissionRule>[] includeRules() default {};

    /**
     * Rules to exclude. Lowest precedence.
     */
    Class<? extends DataPermissionRule>[] excludeRules() default {};

}
