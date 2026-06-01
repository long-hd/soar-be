package com.hdl.soar.framework.common.biz.infra.config;

/**
 * Config API interface for cross-module access.
 * <p>
 * Allows other modules (e.g., system) to read configuration values
 * without depending on the infra module directly.
 */
public interface ConfigCommonApi {

    /**
     * Get config value by key.
     *
     * @param key config key
     * @return config value, or null if not found
     */
    String getConfigValueByKey(String key);

}
