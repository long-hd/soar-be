package com.hdl.soar.framework.websocket.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.tenant.core.util.TenantUtils;
import com.hdl.soar.framework.websocket.core.listener.WebSocketMessageListener;
import com.hdl.soar.framework.websocket.core.message.JsonWebSocketMessage;
import com.hdl.soar.framework.websocket.core.util.WebSocketFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Parses inbound text frames, answers heartbeat pings, and dispatches each message to the
 * listener registered for its type, running the handler within the session's tenant context.
 */
@Slf4j
public class JsonWebSocketMessageHandler extends TextWebSocketHandler {

    /**
     * Type -> listener.
     */
    private final Map<String, WebSocketMessageListener<Object>> listeners = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public JsonWebSocketMessageHandler(List<? extends WebSocketMessageListener> listenersList) {
        listenersList.forEach((Consumer<WebSocketMessageListener>)
                listener -> listeners.put(listener.getType(), listener));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (message.getPayloadLength() == 0) {
            return;
        }
        // Heartbeat: reply to "ping" with "pong".
        if (message.getPayloadLength() == 4 && Objects.equals(message.getPayload(), "ping")) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (Exception ex) {
                log.error("[handleTextMessage][session({}) pong failed]", session.getId(), ex);
            }
            return;
        }
        try {
            JsonWebSocketMessage jsonMessage = JsonUtils.parseObject(message.getPayload(), JsonWebSocketMessage.class);
            if (jsonMessage == null || StrUtil.isEmpty(jsonMessage.getType())) {
                log.error("[handleTextMessage][session({}) message({}) missing type]",
                        session.getId(), message.getPayload());
                return;
            }
            WebSocketMessageListener<Object> listener = listeners.get(jsonMessage.getType());
            if (listener == null) {
                log.error("[handleTextMessage][session({}) no listener for type({})]",
                        session.getId(), jsonMessage.getType());
                return;
            }
            Type type = TypeUtil.getTypeArgument(listener.getClass(), 0);
            Object content = JsonUtils.parseObject(jsonMessage.getContent(), type);
            Long tenantId = WebSocketFrameworkUtils.getTenantId(session);
            // Run within the session owner's tenant context.
            TenantUtils.execute(tenantId, () -> listener.onMessage(session, content));
        } catch (Throwable ex) {
            log.error("[handleTextMessage][session({}) message({}) failed]",
                    session.getId(), message.getPayload(), ex);
        }
    }

}
