package com.hdl.soar.framework.websocket.core.sender.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * Consumes the broadcast (unique group per node) and delivers to this node's local sessions.
 */
@RequiredArgsConstructor
public class KafkaWebSocketMessageConsumer {

    private final KafkaWebSocketMessageSender kafkaWebSocketMessageSender;

    // Unique group per node (group + random UUID) so every node reads every message (broadcast).
    @KafkaListener(
            topics = "${soar.websocket.sender-kafka.topic}",
            groupId = "${soar.websocket.sender-kafka.consumer-group}-#{T(java.util.UUID).randomUUID()}")
    public void onMessage(KafkaWebSocketMessage message) {
        kafkaWebSocketMessageSender.send(message.getSessionId(), message.getUserType(),
                message.getUserId(), message.getMessageType(), message.getMessageContent());
    }

}
