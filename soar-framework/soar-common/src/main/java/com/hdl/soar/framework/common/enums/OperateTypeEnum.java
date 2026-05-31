package com.hdl.soar.framework.common.enums;

import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.core.ArrayValuable;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Operate type enum for API access logs.
 * <p>
 * Auto-inferred from HTTP method when {@code @ApiAccessLog} does not specify explicitly.
 */
@Getter
@AllArgsConstructor
public enum OperateTypeEnum implements ArrayValuable<Integer> {

    /**
     * Query operation.
     */
    GET(1),

    /**
     * Create operation.
     */
    CREATE(2),

    /**
     * Update operation.
     */
    UPDATE(3),

    /**
     * Delete operation.
     */
    DELETE(4),

    /**
     * Export operation.
     */
    EXPORT(5),

    /**
     * Import operation.
     */
    IMPORT(6),

    /**
     * Other operation.
     *
     * <p>Use this type when the operation cannot be categorized into any of the predefined
     * types. The specific operation can still be identified through the operation name.</p>
     */
    OTHER(0);

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OperateTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static OperateTypeEnum of(Integer type) {
        return ArrayUtil.firstMatch(e -> e.getType().equals(type) , OperateTypeEnum.values());
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<OperateTypeEnum> {
        public JpaConverter() {super(OperateTypeEnum.class, OperateTypeEnum::getType);}
    }

}
