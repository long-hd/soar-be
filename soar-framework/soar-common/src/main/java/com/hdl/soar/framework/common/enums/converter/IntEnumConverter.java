package com.hdl.soar.framework.common.enums.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Generic base class for converting Java enums to Integer columns in JPA.
 * <p>
 * Usage: create a concrete converter per enum, passing the getter for the int value.
 * <pre>{@code
 * @Converter(autoApply = true)
 * public class StatusEnumConverter extends IntEnumConverter<StatusEnum> {
 *     public StatusEnumConverter() {
 *         super(StatusEnum.class, StatusEnum::getStatus);
 *     }
 * }
 * }</pre>
 *
 * @param <E> the enum type
 */
@Converter
public abstract class IntEnumConverter<E extends Enum<E>> implements AttributeConverter<E, Integer> {

    private final E[] values;
    private final Function<E, Integer> toInt;

    protected IntEnumConverter(Class<E> clazz, Function<E, Integer> toInt) {
        this.values = clazz.getEnumConstants();
        this.toInt = toInt;
    }

    @Override
    public Integer convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : toInt.apply(attribute);
    }

    @Override
    public E convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {return null;}

        return Arrays.stream(values)
                .filter(e -> toInt.apply(e).equals(dbData))
                .findFirst()
                .orElse(null);
    }
}
