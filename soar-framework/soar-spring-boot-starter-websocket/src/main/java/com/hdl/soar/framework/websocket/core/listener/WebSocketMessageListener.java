package com.hdl.soar.framework.websocket.core.listener;

import org.springframework.web.socket.WebSocketSession;

/**
 * Handles inbound messages of a given type.
 *
 * @param <T> the message content type; the generic argument is used to deserialize content.
 */
public interface WebSocketMessageListener<T> {

    /**
     * @param session the sending session
     * @param message the deserialized message content
     */
    void onMessage(WebSocketSession session, T message);

    /**
     * @return the message type this listener handles (matched against the envelope type)
     */
    String getType();

}