package com.hdl.soar.module.pay.enums;

import com.hdl.soar.framework.common.exception.ErrorCode;

/**
 * Error codes for the pay module.
 * <p>
 * Range convention (following infra {@code 1_001_xxx_xxx} and system {@code 1_002_xxx_xxx}):
 * pay owns {@code 1_003_xxx_xxx}. App uses {@code 1_003_001_xxx}, channel {@code 1_003_002_xxx}.
 */
public interface ErrorCodeConstants {

    // ========== App 1_003_001_xxx ==========
    ErrorCode APP_NOT_FOUND = new ErrorCode(1_003_001_000, "Payment app does not exist");
    ErrorCode APP_KEY_DUPLICATE = new ErrorCode(1_003_001_001, "Payment app key already exists");

    // ========== Channel 1_003_002_xxx ==========
    ErrorCode CHANNEL_NOT_FOUND = new ErrorCode(1_003_002_000, "Payment channel does not exist");
    ErrorCode CHANNEL_EXIST_SAME_CODE = new ErrorCode(1_003_002_001, "This channel already exists under the app");
    ErrorCode CHANNEL_CODE_INVALID = new ErrorCode(1_003_002_002, "Unknown payment channel code");

    // ========== Order 1_003_003_xxx ==========
    ErrorCode ORDER_NOT_FOUND = new ErrorCode(1_003_003_000, "Payment order does not exist");
    ErrorCode ORDER_STATUS_IS_NOT_WAITING = new ErrorCode(1_003_003_001, "Payment order status is not waiting");
    ErrorCode ORDER_STATUS_IS_SUCCESS = new ErrorCode(1_003_003_002, "Payment order is already paid");
    ErrorCode ORDER_IS_EXPIRED = new ErrorCode(1_003_003_003, "Payment order has expired");
    ErrorCode ORDER_CURRENCY_INVALID = new ErrorCode(1_003_003_004, "Unsupported currency");
    ErrorCode ORDER_EXTENSION_NOT_FOUND = new ErrorCode(1_003_003_010, "Payment order extension does not exist");
    ErrorCode ORDER_EXTENSION_STATUS_IS_NOT_WAITING = new ErrorCode(1_003_003_011, "Payment order extension status is not waiting");
    ErrorCode ORDER_EXTENSION_IS_PAID = new ErrorCode(1_003_003_012, "A payment attempt on this order is already paid");

}
