package com.hdl.soar.module.infra.enums;

import com.hdl.soar.framework.common.exception.ErrorCode;

/**
 * Infra module error codes.
 * <p>
 * Code range: [1-001-000-000, 1-002-000-000)
 */
public interface ErrorCodeConstants {

    // ========== API Error Log 1-001-000-000 ==========
    ErrorCode API_ERROR_LOG_NOT_FOUND = new ErrorCode(1_001_000_000, "API error log does not exist");
    ErrorCode API_ERROR_LOG_PROCESSED = new ErrorCode(1_001_000_001, "API error log has already been processed");

}
