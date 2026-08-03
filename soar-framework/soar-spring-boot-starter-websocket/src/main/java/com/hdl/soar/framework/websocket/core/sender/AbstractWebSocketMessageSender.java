package com.hdl.soar.framework.websocket.core.sender;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.websocket.core.message.JsonWebSocketMessage;
import com.hdl.soar.framework.websocket.core.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Resolves target sessions from the {@link WebSocketSessionManager} and writes to the ones
 * held on this node. Broker-backed senders override the public methods to fan out first,
 * then delegate back to {@link #send(String, Integer, Long, String, String)} on each node.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWebSocketMessageSender implements WebSocketMessageSender {

    private final WebSocketSessionManager sessionManager;

    @Override
    public void send(Integer userType, Long userId, String messageType, String messageContent) {
        send(null, userType, userId, messageType, messageContent);
    }

    @Override
    public void send(Integer userType, String messageType, String messageContent) {
        send(null, userType, null, messageType, messageContent);
    }

    @Override
    public void send(String sessionId, String messageType, String messageContent) {
        send(sessionId, null, null, messageType, messageContent);
    }

    /**
     * Resolves the target sessions on this node and delivers to them.
     *
     * @param sessionId      the target session id, or null
     * @param userType       the target user type, or null
     * @param userId         the target user id, or null
     * @param messageType    the message type
     * @param messageContent the message content
     */
    public void send(String sessionId, Integer userType, Long userId, String messageType, String messageContent) {
        List<WebSocketSession> sessions = Collections.emptyList();
        if (StrUtil.isNotEmpty(sessionId)) {
            WebSocketSession session = sessionManager.getSession(sessionId);
            if (session != null) {
                sessions = Collections.singletonList(session);
            }
        } else if (userType != null && userId != null) {
            sessions = (List<WebSocketSession>) sessionManager.getSessionList(userType, userId);
        } else if (userType != null) {
            sessions = (List<WebSocketSession>) sessionManager.getSessionList(userType);
        }
        if (CollUtil.isEmpty(sessions) && log.isDebugEnabled()) {
            log.debug("[send][sessionId({}) userType({}) userId({}) no matching session on this node]",
                    sessionId, userType, userId);
        }
        doSend(sessions, messageType, messageContent);
    }

    protected void doSend(Collection<WebSocketSession> sessions, String messageType, String messageContent) {
        JsonWebSocketMessage message = new JsonWebSocketMessage().setType(messageType).setContent(messageContent);
        String payload = JsonUtils.toJsonString(message);
        sessions.forEach(session -> {
            if (session == null || !session.isOpen()) {
                return;
            }
            try {
                session.sendMessage(new TextMessage(payload));
                log.info("[doSend][session({}) sent type({})]", session.getId(), messageType);
            } catch (IOException ex) {
                log.error("[doSend][session({}) send failed]", session.getId(), ex);
            }
        });
    }

}
