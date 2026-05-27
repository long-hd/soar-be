package com.hdl.soar.module.system.dal.entity.oauth2;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.enums.oauth2.OAuth2GrantTypeEnum;

import com.hdl.soar.framework.jpa.core.converter.JsonStringListConverter;
import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * OAuth2 client Entity
 */
@Entity
@Table(name = "system_oauth2_client")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2ClientPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Client ID
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;

    /**
     * Client secret
     */
    @Column(name = "secret", nullable = false)
    private String secret;

    /**
     * Application name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Application logo
     */
    @Column(name = "logo", nullable = false)
    private String logo;

    /**
     * Application description
     */
    @Column(name = "description")
    private String description;

    /**
     * Status
     * <p>
     * Enum {@link CommonStatusEnum}</p>
     */
    @Column(name = "status", nullable = false)
    private CommonStatusEnum status;

    /**
     * Access token validity period (seconds)
     */
    @Column(name = "access_token_validity_seconds", nullable = false)
    private Integer accessTokenValiditySeconds;

    /**
     * Refresh token validity period (seconds)
     */
    @Column(name = "refresh_token_validity_seconds", nullable = false)
    private Integer refreshTokenValiditySeconds;

    /**
     * Redirect URIs
     */
    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "redirect_uris", nullable = false)
    private List<String> redirectUris;

    /**
     * Authorized grant types
     * <p>
     * Enum {@link OAuth2GrantTypeEnum}</p>
     */
    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "authorized_grant_types", nullable = false)
    private List<String> authorizedGrantTypes;

    /**
     * Scopes
     */
    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "scopes")
    private List<String> scopes;

    /**
     * Auto-approve scopes
     *
     * In code authorization, if the scope is in this list, it will be approved automatically
     */
    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "auto_approve_scopes")
    private List<String> autoApproveScopes;

    /**
     * Authorities (permissions)
     */
    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "authorities")
    private List<String> authorities;

    /**
     * Resource IDs
     */
    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "resource_ids")
    private List<String> resourceIds;

    /**
     * Additional information in JSON format
     */
    @Column(name = "additional_information")
    private String additionalInformation;

}
