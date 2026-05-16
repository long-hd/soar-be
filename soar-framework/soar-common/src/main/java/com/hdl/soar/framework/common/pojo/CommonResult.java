package com.hdl.soar.framework.common.pojo;

import cn.hutool.core.lang.Assert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hdl.soar.framework.common.exception.ErrorCode;
import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil;
import com.hdl.soar.framework.common.exception.ServiceException;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * Generic response
 *
 * @param <T> Generic data type
 */
@Data
public class CommonResult<T> implements Serializable {
    /**
     * Error code
     *
     * @see ErrorCode#getCode()
     */
    private Integer code;

    /**
     * Error message readable by users
     *
     * @see ErrorCode#getMsg()
     */
    private String msg;

    /**
     * Response data
     */
    private T data;

    /**
     * Converts the given result object into another generic result object.
     *
     * Because the CommonResult object returned by method A does not satisfy
     * the return type required by method B, conversion is needed.
     *
     * @param result the input result object
     * @param <T> the returned generic type
     * @return a new CommonResult object
     */
    public static <T> CommonResult<T> error(CommonResult<?> result) {
        return error(result.getCode(), result.getMsg());
    }

    public static <T> CommonResult<T> error(Integer code, String message) {
        Assert.notEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), code, "The code must be wrong! ");
        CommonResult<T> result = new CommonResult<>();
        result.code = code;
        result.msg = message;
        return result;
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode, Object... params) {
        Assert.notEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), errorCode.getCode(), "The code must be wrong! ");
        CommonResult<T> result = new CommonResult<>();
        result.code = errorCode.getCode();
        result.msg = ServiceExceptionUtil.doFormat(errorCode.getCode(), errorCode.getMsg(), params);
        return result;
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMsg());
    }

    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = GlobalErrorCodeConstants.SUCCESS.getCode();
        result.data = data;
        result.msg = "";
        return result;
    }

    public static boolean isSuccess(Integer code) {
        return Objects.equals(code, GlobalErrorCodeConstants.SUCCESS.getCode());
    }

    @JsonIgnore // Prevent Jackson serialization
    public boolean isSuccess() {
        return isSuccess(code);
    }

    @JsonIgnore // Prevent Jackson serialization
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * Checks whether an exception exists. If so, throws a {@link ServiceException}.
     */
    public void checkError() throws ServiceException {
        if (isSuccess()) {
            return;
        }
        // Business exception
        throw new ServiceException(code, msg);
    }

    /**
     * Checks whether an exception exists. If so, throws a {@link ServiceException}.
     * Otherwise, returns the {@link #data} data.
     */
    @JsonIgnore
    public T getCheckedData() {
        checkError();
        return data;
    }

    public static <T> CommonResult<T> error(ServiceException serviceException) {
        return error(serviceException.getCode(), serviceException.getMessage());
    }

}
