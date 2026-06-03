package com.hdl.soar.framework.security.core;

import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hdl.soar.framework.common.enums.UserTypeEnum;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logged-in user information
 */
@Data
@Builder
public class LoginUser {
    public static final String INFO_KEY_NICKNAME = "nickname";
    public static final String INFO_KEY_DEPT_ID = "deptId";

    /**
     * User ID
     */
    private Long id;

    /**
     * User type
     *
     * <p>Related to {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * Additional user information
     */
    private Map<String, String> info;

    /**
     * Tenant ID
     */
    private Long tenantId;

    /**
     * Authorization scopes
     */
    private List<String> scopes;

    /**
     * Expiration time
     */
    private Instant expiresTime;

    // ========== Context ==========

    /**
     * Context fields, not persisted.
     *
     * <p> Used for temporary caching based on the LoginUser dimension.
     */
    @JsonIgnore
    private Map<String, Object> context;

    /**
     * Visited tenant ID
     */
    private Long visitTenantId;

    public <T> T getContext(String key, Class<T> type) { return MapUtil.get(context, key, type); }

    /**
     * Set a context value, lazily initializing the context map.
     *
     * <p>Used to memoize per-request data (e.g. the computed dept data-permission
     * result) on the LoginUser dimension, avoiding repeated lookups within a request.
     */
    public void setContext(String key, Object value) {
        if (context == null) {
            context = new HashMap<>();
        }
        context.put(key, value);
    }

}
