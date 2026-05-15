package com.hdl.soar.framework.common.exception;

import lombok.Data;
import com.hdl.soar.framework.common.exception.enums.ServiceErrorCodeRange;
import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;

/**
 * Error code object.
 *
 * Global error codes occupy [0, 999], see {@link GlobalErrorCodeConstants}.
 * Business exception error codes occupy [1,000,000,000, +∞), see {@link ServiceErrorCodeRange}.
 *
 * TODO: The error code is designed as an object to support future i18n internationalization.
 */
@Data
public class ErrorCode {

    /**
     * Error code
     */
    private final Integer code;
    /**
     * Error message
     */
    private final String msg;

    public ErrorCode(Integer code, String message) {
        this.code = code;
        this.msg = message;
    }

}