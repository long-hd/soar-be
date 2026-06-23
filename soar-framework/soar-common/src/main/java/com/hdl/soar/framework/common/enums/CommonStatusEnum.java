package com.hdl.soar.framework.common.enums;

import com.hdl.soar.framework.common.core.ArrayValuable;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Common status enum.
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum implements ArrayValuable<Integer> {
    ENABLE(0, "ENABLED"),
    DISABLE(1, "DISABLED");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CommonStatusEnum::getStatus).toArray(Integer[]::new);

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<CommonStatusEnum> {
        protected JpaConverter() {
            super(CommonStatusEnum.class, CommonStatusEnum::getStatus);
        }
    }

    /**
     * Status value
     */
    private final Integer status;
    /**
     * Status name
     */
    private final String name;

    @Override
    public Integer[] array() { return ARRAYS; }

    public static CommonStatusEnum of(Integer status) {
        if(status == null) return null;
        for(CommonStatusEnum e : CommonStatusEnum.values()) {
            if(e.getStatus().equals(status)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isEnable(CommonStatusEnum status) {
        return ENABLE.equals(status);
    }

    public static boolean isDisable(CommonStatusEnum status) {
        return DISABLE.equals(status);
    }
}
