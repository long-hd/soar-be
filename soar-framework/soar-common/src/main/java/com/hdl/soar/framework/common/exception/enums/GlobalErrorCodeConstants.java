package com.hdl.soar.framework.common.exception.enums;

import com.hdl.soar.framework.common.exception.ErrorCode;

/**
 * Global error code enum.
 * System exception codes are reserved in the range 0–999.
 *
 * In general, HTTP status codes are used as references:
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
 *
 * Although HTTP status codes are somewhat limited in expressing business semantics,
 * they are still very suitable for system-level usage.
 *
 * A special note: since 0 has traditionally been used to indicate success,
 * we do not use 200 here.
 *
 */
public interface GlobalErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(0, "Success");

    // ========== Client error codes ==========

    ErrorCode BAD_REQUEST = new ErrorCode(400, "Invalid request parameters");
    ErrorCode UNAUTHORIZED = new ErrorCode(401, "Not logged in");
    ErrorCode FORBIDDEN = new ErrorCode(403, "No permission for this operation");
    ErrorCode NOT_FOUND = new ErrorCode(404, "Request not found");
    ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(405, "Unsupported request method");
    ErrorCode LOCKED = new ErrorCode(423, "Request failed, please try again later"); // concurrent request not allowed
    ErrorCode TOO_MANY_REQUESTS = new ErrorCode(429, "Too many requests, please try again later");

    // ========== Server error codes ==========

    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, "System error");
    ErrorCode NOT_IMPLEMENTED = new ErrorCode(501, "Feature not implemented / not enabled");
    ErrorCode ERROR_CONFIGURATION = new ErrorCode(502, "Invalid configuration");

    // ========== Custom error codes ==========

    ErrorCode REPEATED_REQUESTS = new ErrorCode(900, "Duplicate request, please try again later");
    ErrorCode DEMO_DENY = new ErrorCode(901, "Demo mode: write operations are disabled");

    ErrorCode UNKNOWN = new ErrorCode(999, "Unknown error");

}