package com.hdl.soar.module.infra.mq.demo;

import com.hdl.soar.framework.mq.redis.core.stream.AbstractRedisStreamMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Demo stream message.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DemoStreamMessage extends AbstractRedisStreamMessage {

    private String text;

    /**
     * When {@code true}, the listener throws instead of acknowledging, to exercise
     * pending redelivery.
     */
    private Boolean fail;

}
