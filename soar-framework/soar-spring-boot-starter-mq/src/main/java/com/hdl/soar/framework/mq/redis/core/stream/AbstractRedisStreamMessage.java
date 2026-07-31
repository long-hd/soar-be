package com.hdl.soar.framework.mq.redis.core.stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hdl.soar.framework.mq.redis.core.message.AbstractRedisMessage;

/**
 * Base class for Stream messages. Each message type maps to one stream, keyed by
 * the simple class name unless overridden.
 */
public abstract class AbstractRedisStreamMessage extends AbstractRedisMessage {

    /**
     * @return the stream key; defaults to the simple class name.
     */
    @JsonIgnore // routing information, not part of the payload
    public String getStreamKey() {
        return getClass().getSimpleName();
    }

}
