package com.hdl.soar.module.system.framework.datapermission.config;

import com.hdl.soar.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers which system tables participate in department data permission.
 *
 * <p>The presence of a {@link DeptDataPermissionRuleCustomizer} bean activates
 * {@code SoarDeptDataPermissionAutoConfiguration} (which builds the rule).
 */
@Configuration(proxyBeanMethods = false)
public class SoarDataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer systemDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // system_users: filter by dept_id, and "self" = the user's own row (id)
            rule.addDeptColumn("system_users", "dept_id");
            rule.addUserColumn("system_users", "id");
            // NOTE: add more tables here as modules need dept scoping, e.g.
            // rule.addDeptColumn("system_dept", "id");
        };
    }

}