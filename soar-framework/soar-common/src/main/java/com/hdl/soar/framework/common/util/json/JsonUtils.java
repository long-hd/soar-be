package com.hdl.soar.framework.common.util.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON utility class
 */
@Slf4j
public class JsonUtils {
    @Getter
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL); // Ignore null values
    }

    /**
     * Initializes the objectMapper property.
     * <p>
     * In this way, the ObjectMapper Bean created by Spring is used.
     *
     * @param objectMapper the ObjectMapper instance
     */
    public static void init(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
    }

    @SneakyThrows
    public static String toJsonString(Object object) {
        return objectMapper.writeValueAsString(object);
    }
}
