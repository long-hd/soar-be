package com.hdl.soar.framework.common.biz.system.oauth2.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for validating OAuth 2.0 access tokens.
 */
@Data
public class OAuth2AccessTokenCheckRespDTO implements Serializable {
    /**
     * User ID
     */
    private Long userId;

    /**
     * User type
     */
    private Integer userType;

    /**
     * User information
     */
    private Map<String, String> userInfo;

    /**
     * Tenant ID
     */
    private Long tenantId;

    /**
     * Array of authorization scopes
     */
    private List<String> scopes;

    /**
     * Expiration time
     */
    private Instant expiresTime;
}
