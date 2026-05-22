package com.hdl.soar.module.system.enums.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum for login log types
 */
@Getter
@AllArgsConstructor
public enum LoginLogTypeEnum {

    LOGIN_USERNAME(100), // Login using username
    LOGIN_SOCIAL(101),   // Login using social account
    LOGIN_MOBILE(103),   // Login using mobile
    LOGIN_SMS(104),      // Login using SMS

    LOGOUT_SELF(200),    // User-initiated logout
    LOGOUT_DELETE(202);  // Forced logout

    /**
     * Log type
     */
    private final Integer type;

}
