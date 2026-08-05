package com.hdl.soar.module.pay.framework.pay.core.client;

import jakarta.validation.Validator;

/**
 * Marker for a payment channel's configuration (the secret keys and endpoints a client needs).
 * <p>
 * Each rail has its own implementation (e.g. VNPay has its own config). Unlike the reference project,
 * the concrete type is NOT encoded into the stored JSON; it is resolved from the channel code when the
 * client is built (see {@code PayChannelEnum#getConfigClass}). This keeps stored config decoupled from
 * Java class names.
 */
public interface PayClientConfig {

    /**
     * Validate this configuration, throwing if required fields are missing or malformed.
     *
     * @param validator a bean validator
     */
    void validate(Validator validator);

}
