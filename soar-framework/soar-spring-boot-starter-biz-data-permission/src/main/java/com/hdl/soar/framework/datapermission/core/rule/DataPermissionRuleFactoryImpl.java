package com.hdl.soar.framework.datapermission.core.rule;

import com.hdl.soar.framework.datapermission.core.annotation.DataPermission;
import com.hdl.soar.framework.datapermission.core.aop.DataPermissionContextHolder;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default {@link DataPermissionRuleFactory} implementation.
 *
 * <p>Filters rules based on the active {@link DataPermission} annotation in
 * {@link DataPermissionContextHolder}. With no annotation, data permission is on
 * (all rules apply); {@code enable = false} turns it off.
 */
@RequiredArgsConstructor
public class DataPermissionRuleFactoryImpl implements DataPermissionRuleFactory {

    private final List<DataPermissionRule> rules;

    @Override
    public List<DataPermissionRule> getDataPermissionRules() {
        return rules;
    }

    @Override
    public List<DataPermissionRule> getDataPermissionRule() {
        // 1.1 no rules registered
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }
        // 1.2 no annotation -> enabled by default
        DataPermission dataPermission = DataPermissionContextHolder.get();
        if (dataPermission == null) {
            return rules;
        }
        // 1.3 annotated but disabled
        if (!dataPermission.enable()) {
            return Collections.emptyList();
        }
        // 2.1 include subset (highest precedence)
        if (dataPermission.includeRules().length > 0) {
            return rules.stream()
                    .filter(rule -> contains(dataPermission.includeRules(), rule.getClass()))
                    .collect(Collectors.toList());
        }
        // 2.2 exclude subset
        if (dataPermission.excludeRules().length > 0) {
            return rules.stream()
                    .filter(rule -> !contains(dataPermission.excludeRules(), rule.getClass()))
                    .collect(Collectors.toList());
        }
        // 2.3 all rules
        return rules;
    }

    private static boolean contains(Class<? extends DataPermissionRule>[] classes, Class<?> target) {
        for (Class<? extends DataPermissionRule> clazz : classes) {
            if (clazz == target) {
                return true;
            }
        }
        return false;
    }

}
