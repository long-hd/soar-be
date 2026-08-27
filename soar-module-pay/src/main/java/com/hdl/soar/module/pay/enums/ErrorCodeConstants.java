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
    ErrorCode CHANNEL_MOCK_DISABLED = new ErrorCode(1_003_002_003, "Mock payment channel is disabled in this environment");

    // ========== Order 1_003_003_xxx ==========
    ErrorCode ORDER_NOT_FOUND = new ErrorCode(1_003_003_000, "Payment order does not exist");
    ErrorCode ORDER_STATUS_IS_NOT_WAITING = new ErrorCode(1_003_003_001, "Payment order status is not waiting");
    ErrorCode ORDER_STATUS_IS_SUCCESS = new ErrorCode(1_003_003_002, "Payment order is already paid");
    ErrorCode ORDER_IS_EXPIRED = new ErrorCode(1_003_003_003, "Payment order has expired");
    ErrorCode ORDER_CURRENCY_INVALID = new ErrorCode(1_003_003_004, "Unsupported currency");
    ErrorCode ORDER_EXTENSION_NOT_FOUND = new ErrorCode(1_003_003_010, "Payment order extension does not exist");
    ErrorCode ORDER_EXTENSION_STATUS_IS_NOT_WAITING = new ErrorCode(1_003_003_011, "Payment order extension status is not waiting");
    ErrorCode ORDER_EXTENSION_IS_PAID = new ErrorCode(1_003_003_012, "A payment attempt on this order is already paid");

    // ========== Notify 1_003_004_xxx ==========
    ErrorCode NOTIFY_TASK_NOT_FOUND = new ErrorCode(1_003_004_000, "Payment notify task does not exist");

    // ========== Refund 1_003_005_xxx ==========
    ErrorCode REFUND_NOT_FOUND = new ErrorCode(1_003_005_000, "Refund does not exist");
    ErrorCode REFUND_EXISTS = new ErrorCode(1_003_005_001, "A refund with this merchant refund id already exists");
    ErrorCode REFUND_PRICE_EXCEED = new ErrorCode(1_003_005_002, "Refund amount exceeds the paid amount");
    ErrorCode REFUND_HAS_REFUNDING = new ErrorCode(1_003_005_003, "A refund is already in progress for this order");
    ErrorCode REFUND_STATUS_IS_NOT_WAITING = new ErrorCode(1_003_005_004, "Refund status is not waiting");
    ErrorCode REFUND_ORDER_STATUS_INVALID = new ErrorCode(1_003_005_005, "Order cannot be refunded in its current status");

}
