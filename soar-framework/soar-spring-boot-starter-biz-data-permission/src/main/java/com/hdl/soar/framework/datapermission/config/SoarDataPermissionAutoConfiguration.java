package com.hdl.soar.framework.datapermission.config;

import com.hdl.soar.framework.datapermission.core.aop.DataPermissionAnnotationAdvisor;
import com.hdl.soar.framework.datapermission.core.db.DataPermissionStatementInspector;
import com.hdl.soar.framework.datapermission.core.rule.DataPermissionRule;
import com.hdl.soar.framework.datapermission.core.rule.DataPermissionRuleFactory;
import com.hdl.soar.framework.datapermission.core.rule.DataPermissionRuleFactoryImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Core data-permission auto-configuration: rule factory, the Hibernate statement inspector,
 * and the {@code @DataPermission} annotation advisor.
 *
 * @author hdl
 */
@AutoConfiguration
public class SoarDataPermissionAutoConfiguration {

    @Bean
    public DataPermissionRuleFactory dataPermissionRuleFactory(List<DataPermissionRule> rules) {
        return new DataPermissionRuleFactoryImpl(rules);
    }

    @Bean
    public DataPermissionStatementInspector dataPermissionStatementInspector(
            ObjectProvider<DataPermissionRuleFactory> ruleFactoryProvider) {
        return new DataPermissionStatementInspector(ruleFactoryProvider);
    }

    @Bean
    public DataPermissionAnnotationAdvisor dataPermissionAnnotationAdvisor() {
        return new DataPermissionAnnotationAdvisor();
    }

}
