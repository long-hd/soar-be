package com.hdl.soar.module.system.service.oauth2;

import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2ClientPO;

import java.util.Collection;

/**
 * OAuth2.0 Client Service interface.
 * <p>
 * Functionally similar to JdbcClientDetailsService, providing
 * operations for OAuth2 client management.</p>
 */
public interface OAuth2ClientService {

    /**
     * Retrieves an OAuth2 client from cache.
     *
     * @param clientId the client ID
     * @return the OAuth2 client
     */
    OAuth2ClientPO getOAuth2ClientFromCache(String clientId);

    /**
     * Validates whether the client is valid from cache.
     *
     * @return the OAuth2 client
     */
    default OAuth2ClientPO validOAuthClientFromCache(String clientId) {
        return validOAuthClientFromCache(clientId, null, null, null, null);
    }

    /**
     * Validates whether the client is valid from cache.
     *
     * If a parameter is not null, it will be validated.
     *
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @param authorizedGrantType the authorization grant type
     * @param scopes the authorized scopes
     * @param redirectUri the redirect URI
     * @return the OAuth2 client
     */
    OAuth2ClientPO validOAuthClientFromCache(String clientId, String clientSecret, String authorizedGrantType,
                                             Collection<String> scopes, String redirectUri);

}
