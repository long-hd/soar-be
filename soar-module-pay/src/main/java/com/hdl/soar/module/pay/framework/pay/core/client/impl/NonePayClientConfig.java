package com.hdl.soar.module.pay.framework.pay.core.client.impl;

import com.hdl.soar.module.pay.framework.pay.core.client.PayClientConfig;
import jakarta.validation.Validator;
import lombok.Data;

/**
 * Empty configuration, for clients that need no secrets (e.g. the mock client).
 */
@Data
public class NonePayClientConfig implements PayClientConfig {

    @Override
    public void validate(Validator validator) {
        // nothing to validate
    }

}
