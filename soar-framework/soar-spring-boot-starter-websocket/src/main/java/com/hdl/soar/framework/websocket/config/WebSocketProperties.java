package com.hdl.soar.framework.websocket.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * WebSocket configuration.
 */
@Data
@Validated
@ConfigurationProperties("soar.websocket")
public class WebSocketProperties {

    /**
     * The WebSocket handshake path.
     */
    @NotEmpty(message = "WebSocket path must not be empty")
    private String path = "/ws";

    /**
     * The message sender type: {@code local}, {@code redis} (and later rabbitmq/kafka).
     */
    @NotNull(message = "WebSocket sender type must not be null")
    private String senderType = "local";

}
