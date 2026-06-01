package com.hdl.soar.module.infra.enums.logger;


import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.core.ArrayValuable;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * API error log processing status.
 */
@Getter
@AllArgsConstructor
public enum ApiErrorLogProcessStatusEnum implements ArrayValuable<Integer> {

    INIT(0, "Unprocessed"),
    DONE(1, "Processed"),
    IGNORE(2, "Ignored");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ApiErrorLogProcessStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static ApiErrorLogProcessStatusEnum of(Integer status) {
        return ArrayUtil.firstMatch(e -> e.getStatus().equals(status), values());
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<ApiErrorLogProcessStatusEnum> {
        public JpaConverter() {
            super(ApiErrorLogProcessStatusEnum.class, ApiErrorLogProcessStatusEnum::getStatus);
        }
    }

}
