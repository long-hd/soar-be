package com.hdl.soar.module.pay.framework.pay.core.client.impl.vnpay;

import com.hdl.soar.module.pay.framework.pay.core.client.PayClientConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * VNPay channel configuration — the merchant credentials issued by VNPay.
 * <p>
 * Stored as plain JSON in {@code pay_channel.config}, e.g.
 * {@code {"tmnCode":"...","hashSecret":"...","payUrl":"https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"}}.
 */
@Data
public class VnpayPayClientConfig implements PayClientConfig {

    /** VNPay terminal code ({@code vnp_TmnCode}). */
    @NotEmpty(message = "vnp_TmnCode cannot be empty")
    private String tmnCode;

    /** VNPay secret used to sign requests ({@code vnp_HashSecret}). */
    @NotEmpty(message = "vnp_HashSecret cannot be empty")
    private String hashSecret;

    /** VNPay payment gateway URL (sandbox or production). */
    @NotEmpty(message = "payUrl cannot be empty")
    private String payUrl;

    /**
     * VNPay's transaction-query (querydr) API endpoint, e.g.
     * {@code https://sandbox.vnpayment.vn/merchant_webapi/api/transaction}. Distinct from
     * {@code payUrl} (the redirect gateway). Used by {@code getOrder} in the reconcile flow.
     */
    private String queryUrl;

    @Override
    public void validate(Validator validator) {
        Set<ConstraintViolation<VnpayPayClientConfig>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid VNPay config: "
                    + violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; ")));
        }
    }

}
