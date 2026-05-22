package com.hdl.soar.framework.common.exception;

import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Server exception.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ServerException extends RuntimeException {

    /**
     * Global error code.
     *
     * @see GlobalErrorCodeConstants
     */
    private Integer code;
    /**
     * Error message.
     */
    private String message;

    /**
     * No-args constructor to avoid deserialization issues.
     */
    public ServerException() {
    }

    public ServerException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

    public ServerException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServerException setCode(Integer code) {
        this.code = code;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ServerException setMessage(String message) {
        this.message = message;
        return this;
    }

}
