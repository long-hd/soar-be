package com.hdl.soar.framework.websocket.core.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * The envelope sent over the wire: a message type used to route to a listener, and a
 * JSON content payload.
 */
@Data
@Accessors(chain = true)
public class JsonWebSocketMessage implements Serializable {

    /**
     * Message type, used to dispatch to the matching {@code WebSocketMessageListener}.
     */
    private String type;

    /**
     * Message content, expected to be a JSON object.
     */
    private String content;

}
