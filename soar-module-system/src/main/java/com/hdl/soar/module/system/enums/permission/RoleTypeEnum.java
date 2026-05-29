package com.hdl.soar.module.system.enums.permission;

import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleTypeEnum {

    /**
     * Built-in role
     */
    SYSTEM(1),

    /**
     * Custom role
     */
    CUSTOM(2);

    private final Integer type;

    public static RoleTypeEnum of(Integer val) {
        if (val == null) {return null;}
        for (RoleTypeEnum roleTypeEnum : RoleTypeEnum.values()) {
            if (roleTypeEnum.getType().equals(val)) {return roleTypeEnum;}
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<RoleTypeEnum> {
        public JpaConverter() {super(RoleTypeEnum.class, RoleTypeEnum::getType);}
    }

}
