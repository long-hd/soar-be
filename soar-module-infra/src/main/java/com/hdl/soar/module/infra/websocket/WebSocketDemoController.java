package com.hdl.soar.module.infra.websocket;

import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import com.hdl.soar.framework.websocket.core.sender.WebSocketMessageSender;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Manual test: pushes a message to the calling user's own WebSocket sessions.
 */
@RestController
@RequestMapping("/infra/ws-demo")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketDemoController {

    WebSocketMessageSender webSocketMessageSender;

    @GetMapping("/push")
    public String push(@RequestParam String text) {
        LoginUser user = SecurityFrameworkUtils.getLoginUser();
        DemoWebSocketMessage message = new DemoWebSocketMessage();
        message.setText(text);
        webSocketMessageSender.sendObject(user.getUserType(), user.getId(), "demo-push", message);
        return "pushed to userId=" + user.getId();
    }

}
