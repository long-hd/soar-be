package com.hdl.soar.framework.common.util.json;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
        objectMapper.registerModule(new JavaTimeModule());
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

    // ================== Object to Json

    @SneakyThrows
    public static String toJsonString(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows
    public static byte[] toJsonByte(Object object) {
        return objectMapper.writeValueAsBytes(object);
    }

    @SneakyThrows
    public static String toJsonPrettyString(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    // ================== Json to object

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", bytes, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, String path, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            JsonNode treeNode = objectMapper.readTree(text);
            JsonNode pathNode = treeNode.path(path);
            return objectMapper.readValue(pathNode.toString(), clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Type type) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(byte[] text, Type type) {
        if (ArrayUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a string into an object of the specified type.
     *
     * <p>When using {@link #parseObject(String, Class)}, in cases with
     * {@code @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)}, if the text
     * does not contain the class property, it will cause an error.
     * This method avoids that issue.
     *
     * @param text JSON string
     * @param clazz target type
     * @return parsed object
     */
    public static <T> T parseObject2(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        return JSONUtil.toBean(text, clazz);
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse JSON string into an object of the specified type.
     * Returns null if parsing fails.
     *
     * @param text JSON string
     * @param clazz target type
     * @return parsed object, or null if parsing fails
     */
    public static <T> T parseObjectQuietly(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Parse JSON string into an object of the specified type.
     * Returns null if parsing fails.
     *
     * @param text JSON string
     * @param typeReference type reference
     * @return parsed object, or null if parsing fails
     */
    public static <T> T parseObjectQuietly(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String text, String path, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            JsonNode treeNode = objectMapper.readTree(text);
            JsonNode pathNode = treeNode.path(path);
            return objectMapper.readValue(pathNode.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse JSON string to Jackson tree model.
     *
     * @param text JSON string
     * @return JsonNode tree, or null if text is blank
     */
    public static JsonNode parseTree(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (IOException e) {
            log.error("[parseTree][text({}) parse error]", text, e);
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseTree(byte[] text) {
        try {
            return objectMapper.readTree(text);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    // ==================== Convert Object to a Type

    public static boolean isJson(String text) {
        return JSONUtil.isTypeJSON(text);
    }

    /**
     * Check whether the string is a JSON object string
     *
     * @param str input string
     */
    public static boolean isJsonObject(String str) {
        return JSONUtil.isTypeJSONObject(str);
    }

    /**
     * Convert an Object to the target type.
     *
     * <p>
     * This avoids the performance overhead of converting to JSON string
     * and then parsing it back.
     *
     * @param obj   source object (can be Map, POJO, etc.)
     * @param clazz target type
     * @return converted object
     */
    public static <T> T convertObject(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }
        return objectMapper.convertValue(obj, clazz);
    }

    /**
     * Convert an Object to the target type (supports generics).
     *
     * @param obj           source object
     * @param typeReference target type reference
     * @return converted object
     */
    public static <T> T convertObject(Object obj, TypeReference<T> typeReference) {
        if (obj == null) {
            return null;
        }
        return objectMapper.convertValue(obj, typeReference);
    }

    /**
     * Convert an Object to a List type.
     *
     * <p>
     * This avoids the performance overhead of converting to JSON string
     * and then parsing it into a list.
     *
     * @param obj   source object (can be List, array, etc.)
     * @param clazz target element type
     * @return converted List
     */
    public static <T> List<T> convertList(Object obj, Class<T> clazz) {
        if (obj == null) {
            return new ArrayList<>();
        }
        return objectMapper.convertValue(
                obj,
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, clazz)
        );
    }

}
