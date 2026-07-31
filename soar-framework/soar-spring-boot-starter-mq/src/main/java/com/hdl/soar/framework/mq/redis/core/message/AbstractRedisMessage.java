package com.hdl.soar.framework.mq.redis.core.message;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all Redis messages, both Stream and Pub/Sub.
 *
 * <p>Carries the payload (declared by subclasses and JSON-serialized) together
 * with a {@code headers} map for out-of-band metadata, such as a tenant
 * identifier, that travels alongside the payload.
 */
@Data
public class AbstractRedisMessage {

    /**
     * Message headers, serialized together with the payload.
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * @param key header name
     * @return the header value, or {@code null} if absent
     */
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * @param key   header name
     * @param value header value
     */
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

}
