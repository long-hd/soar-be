package com.hdl.soar.framework.websocket.core.session;

import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;

/**
 * Tracks live sessions and looks them up by id, user type, or user.
 */
public interface WebSocketSessionManager {

    void addSession(WebSocketSession session);

    void removeSession(WebSocketSession session);

    WebSocketSession getSession(String id);

    /**
     * @param userType the user type
     * @return sessions of that user type (tenant-filtered by the current context)
     */
    Collection<WebSocketSession> getSessionList(Integer userType);

    /**
     * @param userType the user type
     * @param userId   the user id
     * @return sessions belonging to that user
     */
    Collection<WebSocketSession> getSessionList(Integer userType, Long userId);

}