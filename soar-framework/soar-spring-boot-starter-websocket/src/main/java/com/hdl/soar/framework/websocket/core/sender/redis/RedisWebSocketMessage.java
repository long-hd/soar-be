package com.hdl.soar.framework.websocket.core.sender.redis;

import com.hdl.soar.framework.mq.redis.core.pubsub.AbstractRedisChannelMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * The pub/sub message broadcast to all nodes so the node holding the target session delivers it.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class RedisWebSocketMessage extends AbstractRedisChannelMessage {

    private String sessionId;
    private Integer userType;
    private Long userId;
    private String messageType;
    private String messageContent;

}
