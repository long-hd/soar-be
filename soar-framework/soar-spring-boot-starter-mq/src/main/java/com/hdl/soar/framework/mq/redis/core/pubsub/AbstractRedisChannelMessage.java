package com.hdl.soar.framework.mq.redis.core.pubsub;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hdl.soar.framework.mq.redis.core.message.AbstractRedisMessage;

/**
 * Base class for Pub/Sub messages. Each message type maps to one channel, keyed by
 * the simple class name unless overridden.
 */
public abstract class AbstractRedisChannelMessage extends AbstractRedisMessage {

    /**
     * @return the channel name; defaults to the simple class name.
     */
    @JsonIgnore // routing information, not part of the payload
    public String getChannel() {
        return getClass().getSimpleName();
    }

}
