package com.hdl.soar.module.pay.framework.pay.core.client.dto.refund;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Unified refund request handed to a {@code PayClient}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundUnifiedReqDTO {

    /** External order number (the paid order's winning extension {@code no}). */
    @NotEmpty(message = "outTradeNo cannot be empty")
    private String outTradeNo;

    /** External refund number ({@code PayRefundPO.no}). */
    @NotEmpty(message = "outRefundNo cannot be empty")
    private String outRefundNo;

    /** Refund reason. */
    private String reason;

    /** Amount originally paid. */
    @NotNull(message = "payPrice cannot be null")
    @DecimalMin(value = "0", inclusive = false, message = "payPrice must be positive")
    private BigDecimal payPrice;

    /** Amount to refund. */
    @NotNull(message = "refundPrice cannot be null")
    @DecimalMin(value = "0", inclusive = false, message = "refundPrice must be positive")
    private BigDecimal refundPrice;

    /**
     * Where the channel should call back with the refund result. Optional: rails like VNPay refund
     * synchronously and never call back, so this may be {@code null}.
     */
    private String notifyUrl;

    /**
     * Original pay date — MUST equal the vnp_CreateDate sent at pay time (the winning extension's
     * create time; the A-derive invariant from slice 4). VNPay's vnp_TransactionDate for a refund
     * must match it byte-for-byte or the refund is rejected.
     */
    @NotNull(message = "orderCreateTime cannot be null")
    private Instant orderCreateTime;

    /**
     * Channel-side transaction number of the original payment (VNPay vnp_TransactionNo). The refund
     * API requires it to locate the transaction to refund.
     */
    @NotEmpty(message = "channelOrderNo cannot be empty")
    private String channelOrderNo;

    /** Operator who initiated the refund; maps to VNPay vnp_CreateBy. Required. */
    @NotEmpty(message = "createBy cannot be empty")
    private String createBy;

}
