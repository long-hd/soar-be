package com.hdl.soar.module.infra.mq.demo;

import com.hdl.soar.framework.mq.redis.core.pubsub.AbstractRedisChannelMessage;
import com.hdl.soar.framework.mq.redis.core.pubsub.AbstractRedisChannelMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * Demo channel listener that logs each broadcast message.
 */
@Slf4j
@Component
public class DemoChannelMessageListener extends AbstractRedisChannelMessageListener<DemoChannelMessage> {

    @Override
    public void onMessage(DemoChannelMessage message) {
        log.info("[DemoChannel][received text={}]", message.getText());
    }

}
