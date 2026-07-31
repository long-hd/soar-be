package com.hdl.soar.module.infra.mq.demo;

import com.hdl.soar.framework.mq.redis.core.RedisMQTemplate;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Manual test endpoints for the demo messages.
 */
@Tag(name = "Admin Backend - Demo MQ")
@RestController
@RequestMapping("/infra/mq-demo")
@RequiredArgsConstructor
public class MqDemoController {

    private final RedisMQTemplate redisMQTemplate;

    /**
     * @param text message text
     * @param fail whether the listener should fail
     * @return the appended stream entry id
     */
    @GetMapping("/stream")
    public String sendStream(@RequestParam String text, @RequestParam(required = false) Boolean fail) {
        DemoStreamMessage message = new DemoStreamMessage();
        message.setText(text);
        message.setFail(fail);
        return "sent stream id=" + redisMQTemplate.send(message);
    }

    /**
     * @param text message text
     * @return a confirmation string
     */
    @GetMapping("/pubsub")
    public String sendPubSub(@RequestParam String text) {
        DemoChannelMessage message = new DemoChannelMessage();
        message.setText(text);
        redisMQTemplate.send(message);
        return "sent pubsub";
    }

}
