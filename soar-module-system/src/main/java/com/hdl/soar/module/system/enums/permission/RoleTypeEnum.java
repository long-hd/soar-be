package com.hdl.soar.module.system.enums.permission;

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

}
