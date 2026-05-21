package com.hdl.soar.module.system.service.oauth2;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.system.controller.admin.oauth2.dto.token.OAuth2AccessTokenPageReqDTO;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO;

import java.util.List;
import java.util.Map;

/**
 * OAuth2.0 Token Service interface.
 * <p>
 * Functionally similar to Spring Security OAuth's
 * DefaultTokenServices + JdbcTokenStore, providing operations
 * for access tokens and refresh tokens.
 *</p>
 */
public interface OAuth2TokenService {

    /**
     * Creates an access token.
     * Note: this process also includes creating a refresh token.
     *
     * <p> Reference: createAccessToken method in DefaultTokenServices.
     *
     * @param userId the user ID
     * @param userType the user type
     * @param clientId the client ID
     * @param scopes the authorization scopes
     * @param userInfo additional user information
     * @return the access token information
     */
    OAuth2AccessTokenPO createAccessToken(Long userId, Integer userType, String clientId, List<String> scopes, Map<String, String> userInfo);

    /**
     * Validates an access token.
     *
     * @param accessToken the access token
     * @return the access token information
     */
    OAuth2AccessTokenPO checkAccessToken(String accessToken);


    /**
     * Removes an access token.
     * Note: this process will also remove the related refresh token.
     *
     * Reference: revokeToken method in DefaultTokenServices.
     *
     * @param accessToken the access token (note: originally described as refresh token in comment)
     * @return the access token information
     */
    OAuth2AccessTokenPO removeAccessToken(String accessToken);

    /**
     * Removes access tokens. <br>
     * Note: this process will also remove related refresh tokens.
     * <p>
     * Reference: revokeToken method in DefaultTokenServices.
     *
     * @param userId the user ID
     * @param userType the user type
     */
    void removeAccessToken(Long userId, Integer userType);


    /**
     * Refreshes an access token.
     *
     * <p>Reference: refreshAccessToken method in DefaultTokenServices.
     *
     * @param refreshToken the refresh token
     * @param clientId the client ID
     * @return the access token information
     */
    OAuth2AccessTokenPO refreshAccessToken(String refreshToken, String clientId);

    /**
     * Retrieves an access token.
     *
     * <p>Reference: getAccessToken method in DefaultTokenServices.
     *
     * @param accessToken the access token
     * @return the access token information
     */
    OAuth2AccessTokenPO getAccessToken(String accessToken);

    /**
     * Retrieves a paginated list of access tokens.
     *
     * @param reqDTO the request parameters
     * @return a paginated result of access tokens
     */
    PageResult<OAuth2AccessTokenPO> getAccessTokenPage(OAuth2AccessTokenPageReqDTO reqDTO);

    /**
     * Clean up access tokens that have expired for more than `exceedDay` days
     *
     * @param exceedDay   Number of days after expiration before cleanup
     * @param deleteLimit Number of records to delete per cleanup batch
     */
    Integer cleanAccessToken(Integer exceedDay, Integer deleteLimit);

    /**
     * Clean up refresh tokens that have expired for more than `exceedDay` days
     *
     * @param exceedDay   Number of days after expiration before cleanup
     * @param deleteLimit Number of records to delete per cleanup batch
     */
    Integer cleanRefreshToken(Integer exceedDay, Integer deleteLimit);

}
