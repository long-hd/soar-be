package com.hdl.soar.framework.datapermission.core.rule;

import java.util.List;

/**
 * Factory / container for {@link DataPermissionRule} instances.
 */
public interface DataPermissionRuleFactory {

    /**
     * @return all registered data permission rules
     */
    List<DataPermissionRule> getDataPermissionRules();

    /**
     * Resolve the rules that apply to the current invocation, honoring the
     * active {@code @DataPermission} annotation (enable / includeRules / excludeRules)
     * read from {@code DataPermissionContextHolder}.
     *
     * @return the applicable rules (possibly empty)
     */
    List<DataPermissionRule> getDataPermissionRule();

}
