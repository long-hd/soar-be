package com.hdl.soar.framework.websocket.core.sender.local;

import com.hdl.soar.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import com.hdl.soar.framework.websocket.core.session.WebSocketSessionManager;

/**
 * Single-node sender: delivers directly to sessions on this node.
 */
public class LocalWebSocketMessageSender extends AbstractWebSocketMessageSender {

    public LocalWebSocketMessageSender(WebSocketSessionManager sessionManager) {
        super(sessionManager);
    }

}
