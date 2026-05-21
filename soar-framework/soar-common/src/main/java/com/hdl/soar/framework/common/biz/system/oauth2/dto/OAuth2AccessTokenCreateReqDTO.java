package com.hdl.soar.framework.common.biz.system.oauth2.dto;

import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.validation.InEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating an OAuth2.0 access token.
 */
@Data
public class OAuth2AccessTokenCreateReqDTO {

    /**
     * User ID
     */
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    /**
     * User type
     */
    @NotNull(message = "User type cannot be null")
    @InEnum(value = UserTypeEnum.class, message = "User type must be one of {value}")
    private Integer userType;

    /**
     * Client ID
     */
    @NotNull(message = "Client ID cannot be null")
    private String clientId;

    /**
     * Authorization scopes
     */
    private List<String> scopes;

}
