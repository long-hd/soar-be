package com.hdl.soar.module.infra.mq.demo;

import com.hdl.soar.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Demo stream listener that logs each message and optionally fails.
 */
@Slf4j
@Component
public class DemoStreamMessageListener extends AbstractRedisStreamMessageListener<DemoStreamMessage> {

    @Override
    public void onMessage(DemoStreamMessage message) {
        log.info("[DemoStream][received text={} fail={}]", message.getText(), message.getFail());
        if (Boolean.TRUE.equals(message.getFail())) {
            throw new IllegalStateException("intentional failure to exercise redelivery");
        }
        log.info("[DemoStream][handled, will acknowledge]");
    }

}
