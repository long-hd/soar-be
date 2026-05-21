package com.hdl.soar.framework.common.biz.system.oauth2.dto;

import lombok.Data;

import java.time.Instant;

/**
 * Response DTO containing OAuth2.0 access token information.
 */
@Data
public class OAuth2AccessTokenRespDTO {

    /**
     * Access token
     */
    private String accessToken;

    /**
     * Refresh token
     */
    private String refreshToken;

    /**
     * User ID
     */
    private Long userId;

    /**
     * User type
     */
    private Integer userType;

    /**
     * Expiration time
     */
    private Instant expiresTime;

}
