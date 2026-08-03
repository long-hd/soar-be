package com.hdl.soar.framework.websocket.core.sender.kafka;

import com.hdl.soar.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import com.hdl.soar.framework.websocket.core.session.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Multi-node sender over Kafka: publishes to a topic. Each node consumes with a unique group,
 * so every node receives the message and delivers to its local sessions.
 */
@Slf4j
public class KafkaWebSocketMessageSender extends AbstractWebSocketMessageSender {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String topic;

    public KafkaWebSocketMessageSender(WebSocketSessionManager sessionManager,
                                       KafkaTemplate<Object, Object> kafkaTemplate, String topic) {
        super(sessionManager);
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
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
        KafkaWebSocketMessage mqMessage = new KafkaWebSocketMessage()
                .setSessionId(sessionId).setUserType(userType).setUserId(userId)
                .setMessageType(messageType).setMessageContent(messageContent);
        // Fire-and-forget: a real-time broadcast tolerates loss; do not block the caller on the ack.
        kafkaTemplate.send(topic, mqMessage).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[send][kafka publish failed message({})]", mqMessage, ex);
            }
        });
    }

}
