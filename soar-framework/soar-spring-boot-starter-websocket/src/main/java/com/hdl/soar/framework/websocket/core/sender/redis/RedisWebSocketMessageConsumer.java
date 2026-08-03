package com.hdl.soar.framework.websocket.core.sender.redis;

import com.hdl.soar.framework.mq.redis.core.pubsub.AbstractRedisChannelMessageListener;
import lombok.RequiredArgsConstructor;

/**
 * Receives the broadcast on every node and delivers to this node's local sessions via the
 * sender's node-local {@code send(sessionId, userType, userId, ...)}.
 */
@RequiredArgsConstructor
public class RedisWebSocketMessageConsumer extends AbstractRedisChannelMessageListener<RedisWebSocketMessage> {

    private final RedisWebSocketMessageSender redisWebSocketMessageSender;

    @Override
    public void onMessage(RedisWebSocketMessage message) {
        // Calls the AbstractWebSocketMessageSender node-local resolve+deliver (NOT the overridden broadcast).
        redisWebSocketMessageSender.send(message.getSessionId(), message.getUserType(), message.getUserId(),
                message.getMessageType(), message.getMessageContent());
    }

}
