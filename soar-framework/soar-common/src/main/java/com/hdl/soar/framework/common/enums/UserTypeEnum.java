package com.hdl.soar.framework.common.enums;

import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Global user type enum.
 */
@AllArgsConstructor
@Getter
public enum UserTypeEnum implements ArrayValuable<Integer> {

    MEMBER(1, "Member"), // For C-end users, regular users
    ADMIN(2, "Administrator"); // For B-end, admin backend users

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(UserTypeEnum::getValue).toArray(Integer[]::new);

    /**
     * Type value
     */
    private final Integer value;
    /**
     * Type name
     */
    private final String name;

    public static UserTypeEnum valueOf(Integer value) {
        return ArrayUtil.firstMatch(userType -> userType.getValue().equals(value), UserTypeEnum.values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}

