package com.hdl.soar.framework.websocket.core.security;


import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import com.hdl.soar.framework.websocket.core.util.WebSocketFrameworkUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Binds the authenticated user to the session at handshake time.
 *
 * <p>The token filter has already populated the security context from the handshake
 * request (token via {@code Authorization} header or {@code ?token=} query param, since
 * browsers cannot set headers on a WebSocket). If no user is present, the handshake is
 * rejected.
 */
public class LoginUserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        WebSocketFrameworkUtils.setLoginUser(loginUser, attributes);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }

}
