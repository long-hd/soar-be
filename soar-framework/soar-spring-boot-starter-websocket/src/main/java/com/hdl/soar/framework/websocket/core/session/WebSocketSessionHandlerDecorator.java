package com.hdl.soar.framework.websocket.core.session;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * Registers/deregisters sessions with the {@link WebSocketSessionManager} on connect/close,
 * and wraps each session in a {@link ConcurrentWebSocketSessionDecorator} so concurrent
 * sends are serialized with a send-time and buffer-size limit.
 */
public class WebSocketSessionHandlerDecorator extends WebSocketHandlerDecorator {

    /**
     * Max time allowed for a single send, in milliseconds.
     */
    private static final int SEND_TIME_LIMIT = 1000 * 5;

    /**
     * Max buffered outbound bytes before the session is closed.
     */
    private static final int BUFFER_SIZE_LIMIT = 1024 * 100;

    private final WebSocketSessionManager sessionManager;

    public WebSocketSessionHandlerDecorator(WebSocketHandler delegate, WebSocketSessionManager sessionManager) {
        super(delegate);
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session = new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT, BUFFER_SIZE_LIMIT);
        sessionManager.addSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        sessionManager.removeSession(session);
    }

}
