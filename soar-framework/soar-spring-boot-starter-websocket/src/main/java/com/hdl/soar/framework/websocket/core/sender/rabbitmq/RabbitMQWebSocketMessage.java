package com.hdl.soar.framework.websocket.core.sender.rabbitmq;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * The message broadcast over a RabbitMQ topic exchange so every node's queue receives it.
 */
@Data
@Accessors(chain = true)
public class RabbitMQWebSocketMessage implements Serializable {

    private String sessionId;
    private Integer userType;
    private Long userId;
    private String messageType;
    private String messageContent;

}
