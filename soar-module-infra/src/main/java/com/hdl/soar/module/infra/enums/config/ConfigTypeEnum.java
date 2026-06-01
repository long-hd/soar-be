package com.hdl.soar.module.infra.enums.config;

import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.core.ArrayValuable;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Config type: SYSTEM (built-in, cannot delete) vs CUSTOM (user-created).
 */
@Getter
@AllArgsConstructor
public enum ConfigTypeEnum implements ArrayValuable<Integer> {

    SYSTEM(1, "System Built-in"),
    CUSTOM(2, "Custom");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ConfigTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static ConfigTypeEnum of(Integer type) {
        return ArrayUtil.firstMatch(e -> e.getType().equals(type), values());
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<ConfigTypeEnum> {
        public JpaConverter() {
            super(ConfigTypeEnum.class, ConfigTypeEnum::getType);
        }
    }

}