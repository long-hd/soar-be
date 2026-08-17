package com.hdl.soar.module.pay.api.refund.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Refund creation request (in-process API). A business module calls this to refund one of its paid
 * orders.
 */
@Data
public class PayRefundCreateReqDTO {

    /** App identity of the caller. */
    @NotEmpty(message = "appKey cannot be empty")
    private String appKey;

    /** Client IP that requested the refund. */
    @NotEmpty(message = "userIp cannot be empty")
    private String userIp;

    /** Merchant's own order id (identifies which paid order to refund). */
    @NotEmpty(message = "merchantOrderId cannot be empty")
    private String merchantOrderId;

    /** Merchant's own refund id — unique per app; the idempotency key. */
    @NotEmpty(message = "merchantRefundId cannot be empty")
    private String merchantRefundId;

    /** Refund reason. */
    @NotEmpty(message = "reason cannot be empty")
    @Size(max = 128, message = "reason must be at most 128 characters")
    private String reason;

    /** Amount to refund. */
    @NotNull(message = "price cannot be null")
    @DecimalMin(value = "0", inclusive = false, message = "price must be positive")
    private BigDecimal price;

    /** Merchant-side user who initiated this refund (VNPay vnp_CreateBy). Required by VNPay. */
    @NotEmpty(message = "createBy cannot be empty")
    @Size(max = 255, message = "createBy must be at most 255 characters")
    private String createBy;

}
