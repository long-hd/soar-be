package com.hdl.soar.framework.tenant.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Registers {@link TenantProperties} unconditionally so that any module
 * can constructor-inject it regardless of whether the tenant infrastructure
 * is enabled or disabled.
 */
@AutoConfiguration
@EnableConfigurationProperties(TenantProperties.class)
public class SoarTenantPropertiesAutoConfiguration {
}
