package com.hdl.soar.framework.datapermission.core.rule.dept;

/**
 * Customizer for {@link DeptDataPermissionRule}.
 *
 * <p>Implementations register the per-table column configuration:
 * <ul>
 *   <li>{@link DeptDataPermissionRule#addDeptColumn(String, String)} — dept-based filtering</li>
 *   <li>{@link DeptDataPermissionRule#addUserColumn(String, String)} — self/user-based filtering</li>
 * </ul>
 */
@FunctionalInterface
public interface DeptDataPermissionRuleCustomizer {

    /**
     * Customize the given rule (register tables + columns).
     *
     * @param rule the dept data permission rule
     */
    void customize(DeptDataPermissionRule rule);

}