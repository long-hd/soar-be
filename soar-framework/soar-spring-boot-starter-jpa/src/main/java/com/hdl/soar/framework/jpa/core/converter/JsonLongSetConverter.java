package com.hdl.soar.framework.jpa.core.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Set;

/**
 * Converts {@code Set<Long>} to/from JSON string for DB storage.
 *
 * <p>DB stores: {@code [1, 2, 3]} (JSON array in varchar/text column).
 * Java side: {@code Set<Long>}.
 *
 * <p>Not using {@code autoApply} because this converter is for a specific
 * use case (JSON column), not all {@code Set<Long>} fields.
 */
@Converter
public class JsonLongSetConverter implements AttributeConverter<Set<Long>, String> {

    private static final TypeReference<Set<Long>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Set<Long> attribute) {
        return attribute == null ? null : JsonUtils.toJsonString(attribute);
    }

    @Override
    public Set<Long> convertToEntityAttribute(String dbData) {
        return dbData == null ? null :  JsonUtils.parseObject(dbData, TYPE_REF);
    }
}
