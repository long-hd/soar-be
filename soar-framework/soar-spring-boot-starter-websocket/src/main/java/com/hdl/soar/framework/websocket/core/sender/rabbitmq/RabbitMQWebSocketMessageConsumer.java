package com.hdl.soar.framework.websocket.core.sender.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;

/**
 * Declares a per-node auto-delete queue bound to the shared topic exchange, receives the
 * broadcast, and delivers to this node's local sessions.
 */
@RabbitListener(
        bindings = @QueueBinding(
                value = @Queue(
                        name = "${soar.websocket.sender-rabbitmq.queue}-#{T(java.util.UUID).randomUUID()}",
                        autoDelete = "true"),
                exchange = @Exchange(
                        name = "${soar.websocket.sender-rabbitmq.exchange}",
                        type = ExchangeTypes.TOPIC,
                        declare = "false"),
                key = "#"))
@RequiredArgsConstructor
public class RabbitMQWebSocketMessageConsumer {

    private final RabbitMQWebSocketMessageSender rabbitMQWebSocketMessageSender;

    @RabbitHandler
    public void onMessage(RabbitMQWebSocketMessage message) {
        // node-local resolve + deliver
        rabbitMQWebSocketMessageSender.send(message.getSessionId(), message.getUserType(),
                message.getUserId(), message.getMessageType(), message.getMessageContent());
    }

}
