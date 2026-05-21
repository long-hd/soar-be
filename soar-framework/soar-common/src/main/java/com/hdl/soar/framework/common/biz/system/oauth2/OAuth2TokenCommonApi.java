package com.hdl.soar.framework.common.biz.system.oauth2;

import com.hdl.soar.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import com.hdl.soar.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import com.hdl.soar.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import jakarta.validation.Valid;

/**
 * OAuth 2.0 Token API interface
 */
public interface OAuth2TokenCommonApi {

    /**
     * Creates an access token.
     *
     * @param reqDTO the information used to create the access token
     * @return the access token information
     */
    OAuth2AccessTokenRespDTO createAccessToken(@Valid OAuth2AccessTokenCreateReqDTO reqDTO);

    /**
     * Validate the access token.
     *
     * @param accessToken the access token
     * @return information of the access token
     */
    OAuth2AccessTokenCheckRespDTO checkAccessToken(String accessToken);

    /**
     * Removes an access token.
     *
     * @param accessToken the access token
     * @return the access token information
     */
    OAuth2AccessTokenRespDTO removeAccessToken(String accessToken);

    /**
     * Refreshes an access token.
     *
     * @param refreshToken the refresh token
     * @param clientId the client ID
     * @return the access token information
     */
    OAuth2AccessTokenRespDTO refreshAccessToken(String refreshToken, String clientId);

}
