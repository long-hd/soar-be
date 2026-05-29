package com.hdl.soar.module.system.enums.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Menu type enum
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    DIR(1), // Directory
    MENU(2), // Menu
    BUTTON(3); // Button

    /**
     * Type
     */
    private final Integer type;

    public static MenuTypeEnum of(Integer type) {
        if (type == null) {return null;}
        for (MenuTypeEnum e : MenuTypeEnum.values()) {
            if (e.type.equals(type)) {return e;}
        }
        return null;
    }

}
