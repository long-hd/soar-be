package com.hdl.soar.framework.websocket.core.sender.rabbitmq;

import com.hdl.soar.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import com.hdl.soar.framework.websocket.core.session.WebSocketSessionManager;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Multi-node sender over RabbitMQ: publishes to a topic exchange. Each node binds its own
 * auto-delete queue, so every node receives the message and delivers to its local sessions.
 */
public class RabbitMQWebSocketMessageSender extends AbstractWebSocketMessageSender {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange topicExchange;

    public RabbitMQWebSocketMessageSender(WebSocketSessionManager sessionManager,
                                          RabbitTemplate rabbitTemplate, TopicExchange topicExchange) {
        super(sessionManager);
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
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
        RabbitMQWebSocketMessage mqMessage = new RabbitMQWebSocketMessage()
                .setSessionId(sessionId).setUserType(userType).setUserId(userId)
                .setMessageType(messageType).setMessageContent(messageContent);
        // empty routing key: a topic exchange with per-node queues bound to "#" delivers to all.
        rabbitTemplate.convertAndSend(topicExchange.getName(), "", mqMessage);
    }

}
