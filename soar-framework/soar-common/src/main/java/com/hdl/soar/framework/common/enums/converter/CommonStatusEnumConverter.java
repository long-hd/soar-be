package com.hdl.soar.framework.common.enums.converter;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true)
public class CommonStatusEnumConverter implements AttributeConverter<CommonStatusEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CommonStatusEnum attribute) {
        return attribute == null ? null : attribute.getStatus();
    }

    @Override
    public CommonStatusEnum convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : Arrays.stream(CommonStatusEnum.values())
                .filter(e -> e.getStatus().equals(dbData))
                .findFirst()
                .orElse(null);
    }
}
