package com.hdl.soar.module.infra.mq.demo;

import com.hdl.soar.framework.mq.redis.core.pubsub.AbstractRedisChannelMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Demo channel message.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DemoChannelMessage extends AbstractRedisChannelMessage {

    private String text;

}
