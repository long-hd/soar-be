package com.hdl.soar.module.system.dal.entity.oauth2;

import com.hdl.soar.framework.common.enums.UserTypeEnum;

import com.hdl.soar.framework.jpa.core.converter.JsonStringListConverter;
import com.hdl.soar.framework.tenant.core.db.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * OAuth2 refresh token
 */
@Entity
@Table(name = "system_oauth2_access_token")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2RefreshTokenEntity extends TenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
     * Client ID
     * <p>
     * References {@link OAuth2ClientEntity#getClientId()}</p>
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;

    /**
     * Authorization scopes
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
