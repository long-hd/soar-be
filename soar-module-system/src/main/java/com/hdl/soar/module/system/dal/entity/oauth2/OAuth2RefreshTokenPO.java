package com.hdl.soar.module.system.dal.entity.oauth2;

import com.hdl.soar.framework.common.enums.UserTypeEnum;

import com.hdl.soar.framework.jpa.core.converter.JsonStringListConverter;
import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

/**
 * OAuth2 refresh token
 */
@Entity
@Table(name = "system_oauth2_refresh_token")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2RefreshTokenPO extends TenantBasePO {

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
     * References {@link OAuth2ClientPO#getClientId()}</p>
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
