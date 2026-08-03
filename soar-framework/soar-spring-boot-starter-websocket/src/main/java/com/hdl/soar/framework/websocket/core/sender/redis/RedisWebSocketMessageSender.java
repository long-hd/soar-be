package com.hdl.soar.framework.websocket.core.sender.redis;

import com.hdl.soar.framework.mq.redis.core.RedisMQTemplate;
import com.hdl.soar.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import com.hdl.soar.framework.websocket.core.session.WebSocketSessionManager;

/**
 * Multi-node sender: instead of delivering locally, broadcasts the message over Redis pub/sub.
 * Every node's {@link RedisWebSocketMessageConsumer} then delivers to its own local sessions.
 */
public class RedisWebSocketMessageSender extends AbstractWebSocketMessageSender {

    private final RedisMQTemplate redisMQTemplate;

    public RedisWebSocketMessageSender(WebSocketSessionManager sessionManager, RedisMQTemplate redisMQTemplate) {
        super(sessionManager);
        this.redisMQTemplate = redisMQTemplate;
    }

    @Override
    public void send(Integer userType, Long userId, String messageType, String messageContent) {
        broadcast(null, userType, userId, messageType, messageContent);
    }

    @Override
    public void send(Integer userType, String messageType, String messageContent) {
        broadcast(null, userType, null, messageType, messageContent);
    }

    @Override
    public void send(String sessionId, String messageType, String messageContent) {
        broadcast(sessionId, null, null, messageType, messageContent);
    }

    private void broadcast(String sessionId, Integer userType, Long userId,
                           String messageType, String messageContent) {
        RedisWebSocketMessage mqMessage = new RedisWebSocketMessage()
                .setSessionId(sessionId).setUserType(userType).setUserId(userId)
                .setMessageType(messageType).setMessageContent(messageContent);
        redisMQTemplate.send(mqMessage);
    }

}
