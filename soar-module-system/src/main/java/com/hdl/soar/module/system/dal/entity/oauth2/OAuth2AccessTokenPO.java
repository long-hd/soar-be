package com.hdl.soar.module.system.dal.entity.oauth2;

import com.hdl.soar.framework.common.enums.UserTypeEnum;

import com.hdl.soar.framework.jpa.core.converter.JsonStringListConverter;
import com.hdl.soar.framework.jpa.core.converter.JsonStringMapConverter;
import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 access token entity.
 * <p>
 *  The following fields are currently not used and are not supported at the moment:
 *  user_name, authentication (user information)
 * </p>
 */
@Entity
@Table(name = "system_oauth2_access_token")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2AccessTokenPO extends TenantBasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Access token
     */
    @Column(name = "access_token", nullable = false)
    private String accessToken;

    /**
     * Refresh token
     */
    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    /**
     * User ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * User type
     * <p>
     * Enum {@link UserTypeEnum}</p>
     */
    @Column(name = "user_type", nullable = false)
    private Integer userType;

    /**
     * User information
     * <p>
     *     Additional user info stored as JSON object (e.g., {@code {"nickname":"admin","deptId":"1"}}).
     * </p>
     */
    @Convert(converter = JsonStringMapConverter.class)
    @Column(name = "user_info")
    private Map<String, String> userInfo;

    /**
     * Client ID
     *
     * References {@link OAuth2ClientPO#getClientId()}
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;

    /**
     * Scopes
     */
    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "scopes")
    private List<String> scopes;

    /**
     * Expiration time
     */
    @Column(name = "expires_time", nullable = false)
    private Instant expiresTime;

}
