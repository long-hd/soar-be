package com.hdl.soar.framework.websocket.core.sender.kafka;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * The message broadcast over a Kafka topic so every node (each in its own consumer group) receives it.
 */
@Data
@Accessors(chain = true)
public class KafkaWebSocketMessage {

    private String sessionId;
    private Integer userType;
    private Long userId;
    private String messageType;
    private String messageContent;

}
