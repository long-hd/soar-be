package com.hdl.soar.module.pay.api.notify;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Body POSTed to the merchant's refund callback. Like {@code PayOrderNotifyReqDTO}, this is an
 * at-least-once signal — the merchant must re-query for the authoritative result and stay idempotent
 * on {@code merchantRefundId}.
 */
@Data
public class PayRefundNotifyReqDTO {

    @NotBlank(message = "merchantOrderId cannot be empty")
    private String merchantOrderId;

    @NotBlank(message = "merchantRefundId cannot be empty")
    private String merchantRefundId;

    @NotNull(message = "payRefundId cannot be null")
    private Long payRefundId;

}
