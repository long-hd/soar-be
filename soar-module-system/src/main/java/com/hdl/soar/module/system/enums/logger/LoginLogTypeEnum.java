package com.hdl.soar.module.system.enums.logger;

import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
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

    public static LoginLogTypeEnum of(Integer val) {
        if(val == null) {return null;}
        for (LoginLogTypeEnum logType : LoginLogTypeEnum.values()) {
            if(logType.getType().equals(val)) {
                return logType;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<LoginLogTypeEnum> {
        protected JpaConverter() {
            super(LoginLogTypeEnum.class, LoginLogTypeEnum::getType);
        }
    }

}
