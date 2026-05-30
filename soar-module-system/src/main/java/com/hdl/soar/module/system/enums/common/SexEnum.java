package com.hdl.soar.module.system.enums.common;

import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Gender enum
 */
@Getter
@AllArgsConstructor
public enum SexEnum {

    /** Male */
    MALE(1),

    /** Female */
    FEMALE(2),

    /** Unknown */
    UNKNOWN(0);

    /**
     * Gender value
     */
    private final Integer sex;

    public static SexEnum of(Integer value) {
        if (value == null) {return null;}
        for (SexEnum e : SexEnum.values()) {
            if(e.getSex().equals(value)){
                return e;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<SexEnum> {
        public JpaConverter() {super(SexEnum.class, SexEnum::getSex);}
    }

}
