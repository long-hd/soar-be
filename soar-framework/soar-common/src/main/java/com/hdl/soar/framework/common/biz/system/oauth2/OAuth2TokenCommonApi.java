package com.hdl.soar.framework.common.biz.system.oauth2;

import com.hdl.soar.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;

/**
 * OAuth 2.0 Token API interface
 */
public interface OAuth2TokenCommonApi {


    /**
     * Validate the access token.
     *
     * @param accessToken the access token
     * @return information of the access token
     */
    OAuth2AccessTokenCheckRespDTO checkAccessToken(String accessToken);
}
