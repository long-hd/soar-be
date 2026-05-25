package com.hdl.soar.framework.jpa.core.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Generic base converter for storing Java objects as JSON strings in database columns.
 *
 * <p>Subclasses only need to declare the target type in their {@code extends} clause —
 * no constructor, no boilerplate. The base class resolves the actual type at runtime
 * via reflection on the subclass declaration.
 *
 * <p><b>How it works:</b>
 * Java type erasure removes generic info from instances at runtime, but preserves it
 * in class declarations. When a subclass declares:
 * <pre>{@code
 * public class JsonLongSetConverter extends AbstractJsonConverter<Set<Long>> {}
 * }</pre>
 * The compiler writes {@code Set<Long>} into the bytecode. At runtime,
 * {@code getClass().getGenericSuperclass()} returns {@code AbstractJsonConverter<Set<Long>>},
 * and {@code getActualTypeArguments()[0]} extracts {@code Set<Long>}.
 * This is used to construct a {@link TypeReference} for Jackson deserialization.
 *
 * <p><b>Usage — adding a new JSON column type:</b>
 * <pre>{@code
 * @Converter
 * public class JsonLongSetConverter extends AbstractJsonConverter<Set<Long>> {}
 * }</pre>
 * Then on the entity field:
 * <pre>{@code
 * @Convert(converter = JsonLongSetConverter.class)
 * @Column(name = "post_ids")
 * private Set<Long> postIds;
 * }</pre>
 *
 * <p><b>Why not a single generic converter class?</b>
 * JPA requires each {@code @Converter} to be a concrete class — it instantiates converters
 * by class, not by instance. So each JSON column type needs its own class, but the logic
 * lives entirely here.
 *
 * @param <T> the Java type to convert (e.g., {@code Set<Long>}, {@code List<String>}, {@code Map<String, String>})
 */
@Converter
public abstract class AbstractJsonConverter<T> implements AttributeConverter<T, String> {

    private final TypeReference<T> typeReference;

    /**
     * Resolves the actual generic type argument from the subclass declaration.
     *
     * <p>For example, if the subclass is:
     * <pre>{@code
     * class JsonLongSetConverter extends AbstractJsonConverter<Set<Long>> {}
     * }</pre>
     * Then {@code actualType} resolves to {@code Set<Long>}, and the constructed
     * {@link TypeReference} tells Jackson exactly how to deserialize the JSON string.
     */
    protected AbstractJsonConverter() {
        Type superClass = getClass().getGenericSuperclass();
        Type actualType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        this.typeReference = new TypeReference<>() {
            @Override
            public Type getType() {
                return actualType;
            }
        };
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return attribute == null ? null : JsonUtils.toJsonString(attribute);
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ? null : JsonUtils.parseObject(dbData, typeReference);
    }
}
