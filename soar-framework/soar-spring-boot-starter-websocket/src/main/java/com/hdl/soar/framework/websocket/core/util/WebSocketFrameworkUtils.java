package com.hdl.soar.framework.websocket.core.util;

import com.hdl.soar.framework.security.core.LoginUser;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * Helpers for reading the {@link LoginUser} bound to a WebSocket session at handshake time.
 */
public class WebSocketFrameworkUtils {

    public static final String ATTRIBUTE_LOGIN_USER = "LOGIN_USER";

    /**
     * @param loginUser  the authenticated user
     * @param attributes the handshake attributes, later copied into the session
     */
    public static void setLoginUser(LoginUser loginUser, Map<String, Object> attributes) {
        attributes.put(ATTRIBUTE_LOGIN_USER, loginUser);
    }

    public static LoginUser getLoginUser(WebSocketSession session) {
        return (LoginUser) session.getAttributes().get(ATTRIBUTE_LOGIN_USER);
    }

    public static Long getLoginUserId(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getId() : null;
    }

    public static Integer getLoginUserType(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getUserType() : null;
    }

    public static Long getTenantId(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getTenantId() : null;
    }

}
