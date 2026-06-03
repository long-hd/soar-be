package com.hdl.soar.framework.datapermission.config;

import com.hdl.soar.framework.common.biz.system.permission.PermissionCommonApi;
import com.hdl.soar.framework.datapermission.core.rule.dept.DeptDataPermissionRule;
import com.hdl.soar.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import com.hdl.soar.framework.security.core.LoginUser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Department-based data-permission auto-configuration.
 *
 * <p>Only active when at least one {@link DeptDataPermissionRuleCustomizer} bean is present
 * (a module declares which tables/columns participate).
 *
 * @author hdl
 */
@AutoConfiguration
@ConditionalOnClass(LoginUser.class)
@ConditionalOnBean(DeptDataPermissionRuleCustomizer.class)
public class SoarDeptDataPermissionAutoConfiguration {

    @Bean
    public DeptDataPermissionRule deptDataPermissionRule(PermissionCommonApi permissionApi,
                                                         List<DeptDataPermissionRuleCustomizer> customizers) {
        DeptDataPermissionRule rule = new DeptDataPermissionRule(permissionApi);
        customizers.forEach(customizer -> customizer.customize(rule));
        return rule;
    }

}