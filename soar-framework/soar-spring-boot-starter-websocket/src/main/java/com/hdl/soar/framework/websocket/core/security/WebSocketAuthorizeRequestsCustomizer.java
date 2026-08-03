package com.hdl.soar.framework.websocket.core.security;

import com.hdl.soar.framework.security.config.AuthorizeRequestsCustomizer;
import com.hdl.soar.framework.web.config.WebProperties;
import com.hdl.soar.framework.websocket.config.WebSocketProperties;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Permits the WebSocket handshake path. The handshake is gated by
 * {@link LoginUserHandshakeInterceptor} instead of the security config, because the token
 * filter still runs and populates the user even on a permitted path.
 */
public class WebSocketAuthorizeRequestsCustomizer extends AuthorizeRequestsCustomizer {

    private final WebSocketProperties webSocketProperties;

    public WebSocketAuthorizeRequestsCustomizer(WebProperties webProperties, WebSocketProperties webSocketProperties) {
        super(webProperties);
        this.webSocketProperties = webSocketProperties;
    }

    @Override
    public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers(webSocketProperties.getPath()).permitAll();
    }

}