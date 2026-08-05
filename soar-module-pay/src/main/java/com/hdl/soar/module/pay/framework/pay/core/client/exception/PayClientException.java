package com.hdl.soar.module.pay.framework.pay.core.client.exception;

/**
 * Raised when a channel client fails to talk to its rail.
 * <p>
 * This is an infrastructure-level failure (network, signature, malformed channel response), distinct
 * from a business {@code ServiceException}. It is not mapped to a business error code.
 */
public class PayClientException extends RuntimeException {

    public PayClientException(String message) {
        super(message);
    }

    public PayClientException(Throwable cause) {
        super(cause);
    }

}
