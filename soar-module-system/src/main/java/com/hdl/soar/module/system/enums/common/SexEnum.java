package com.hdl.soar.module.system.enums.common;

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
}
