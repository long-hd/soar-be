package com.hdl.soar.module.system.enums.permission;

import com.hdl.soar.framework.common.core.ArrayValuable;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Data scope enum
 *
 * <p>Used to implement data-level permission control
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum implements ArrayValuable<Integer> {

    ALL(1), // Full data access permission

    DEPT_CUSTOM(2), // Custom department data access
    DEPT_ONLY(3), // Department-only data access
    DEPT_AND_CHILD(4), // Department and sub-departments data access

    SELF(5); // Own data only

    /**
     * Scope value
     */
    private final Integer scope;

    public static DataScopeEnum of(Integer val) {
        if (val == null) {return null;}
        for (DataScopeEnum e : DataScopeEnum.values()) {
            if (e.getScope().equals(val)) {return e;}
        }
        return null;
    }

    public static final Integer[] ARRAYS =
            Arrays.stream(values())
                    .map(DataScopeEnum::getScope)
                    .toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<DataScopeEnum> {
        protected JpaConverter() {super(DataScopeEnum.class, DataScopeEnum::getScope);}
    }

}
