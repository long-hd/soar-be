package com.hdl.soar.module.infra.websocket;

import com.hdl.soar.framework.websocket.core.listener.WebSocketMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class DemoWebSocketMessageListener implements WebSocketMessageListener<DemoWebSocketMessage> {

    @Override
    public void onMessage(WebSocketSession session, DemoWebSocketMessage message) {
        log.info("[DemoWebSocket][session({}) received text={}]", session.getId(), message.getText());
    }

    @Override
    public String getType() {
        return "demo";
    }

}
