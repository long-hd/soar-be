package com.hdl.soar.framework.websocket.core.sender;

import com.hdl.soar.framework.common.util.json.JsonUtils;

/**
 * Sends a message to WebSocket clients, addressed by session, by user, or by user type.
 */
public interface WebSocketMessageSender {

    /**
     * @param userType       the target user type
     * @param userId         the target user id
     * @param messageType    the message type
     * @param messageContent the message content, JSON
     */
    void send(Integer userType, Long userId, String messageType, String messageContent);

    /**
     * @param userType       the target user type (all users of this type)
     * @param messageType    the message type
     * @param messageContent the message content, JSON
     */
    void send(Integer userType, String messageType, String messageContent);

    /**
     * @param sessionId      the target session id
     * @param messageType    the message type
     * @param messageContent the message content, JSON
     */
    void send(String sessionId, String messageType, String messageContent);

    default void sendObject(Integer userType, Long userId, String messageType, Object messageContent) {
        send(userType, userId, messageType, JsonUtils.toJsonString(messageContent));
    }

    default void sendObject(Integer userType, String messageType, Object messageContent) {
        send(userType, messageType, JsonUtils.toJsonString(messageContent));
    }

    default void sendObject(String sessionId, String messageType, Object messageContent) {
        send(sessionId, messageType, JsonUtils.toJsonString(messageContent));
    }

}
