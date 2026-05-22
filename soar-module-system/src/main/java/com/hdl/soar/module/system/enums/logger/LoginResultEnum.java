package com.hdl.soar.module.system.enums.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum class for login results
 */
@Getter
@AllArgsConstructor
public enum LoginResultEnum {

    SUCCESS(0),              // Success
    BAD_CREDENTIALS(10),     // Incorrect username or password
    USER_DISABLED(20),       // User is disabled
    CAPTCHA_NOT_FOUND(30),   // Captcha not found
    CAPTCHA_CODE_ERROR(31);  // Incorrect captcha

    /**
     * Result code
     */
    private final Integer result;

}
