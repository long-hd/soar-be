package com.hdl.soar.module.system.enums.oauth2;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2 grant type (authorization mode) enum
 */
@Getter
@AllArgsConstructor
public enum OAuth2GrantTypeEnum {

    PASSWORD("password"), // Password mode
    AUTHORIZATION_CODE("authorization_code"), // Authorization code mode
    IMPLICIT("implicit"), // Implicit mode
    CLIENT_CREDENTIALS("client_credentials"), // Client credentials mode
    REFRESH_TOKEN("refresh_token"), // Refresh token mode
    ;

    private final String grantType;

    public static OAuth2GrantTypeEnum getByGrantType(String grantType) {
        return ArrayUtil.firstMatch(o -> o.getGrantType().equals(grantType), values());
    }

}
