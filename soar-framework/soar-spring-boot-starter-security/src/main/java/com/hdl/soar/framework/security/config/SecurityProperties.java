package com.hdl.soar.framework.security.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "soar.security")
public class SecurityProperties {
    /**
     * HTTP request header used for access token transmission
     */
    @NotEmpty(message = "Token Header cannot be empty")
    private String tokenHeader = "Authorization";

    /**
     * HTTP request parameter used for access token transmission
     * <p>
     * Originally introduced to support WebSocket,
     * since WebSocket cannot pass headers and must pass tokens via URL parameters.
     */
    @NotEmpty(message = "Token Parameter cannot be empty")
    private String tokenParameter = "token";

    /**
     * Switch for mock mode
     */
    @NotNull(message = "Mock mode flag cannot be null")
    private Boolean mockEnable = false;

    /**
     * Secret key for mock mode
     * Must be configured to ensure security
     */
    @NotEmpty(message = "Mock mode secret cannot be empty")
    // Default value provided because it is only required when mockEnable is true
    private String mockSecret = "test";

    /**
     * List of URLs that do not require authentication
     */
    private List<String> permitAllUrls = Collections.emptyList();

    /**
     * PasswordEncoder strength level; higher value means higher computational cost
     */
    private Integer passwordEncoderLength = 4;
}
