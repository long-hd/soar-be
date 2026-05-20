package com.hdl.soar.framework.jpa.core.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

/**
 * Converts {@code Map<String, String>} to/from JSON string.
 * DB: {@code {"nickname":"admin"}} — Java: {@code Map<String, String>}
 */
@Converter
public class JsonStringMapConverter implements AttributeConverter<Map<String, String>, String> {

    private static final TypeReference<Map<String, String>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        return attribute == null ? null : JsonUtils.toJsonString(attribute);
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        return dbData == null ? null : JsonUtils.parseObject(dbData, TYPE_REF);
    }
}
