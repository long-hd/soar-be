package com.hdl.soar.module.pay.framework.pay.core.client.dto.refund;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Flat request to query a refund's status at the channel (reconcile path).
 * <p>
 * Flat, not a per-rail subclass: mirrors {@code PayOrderGetReqDTO}. A rail that doesn't need a field
 * ignores it (mock ignores {@code createTime}); if rail inputs ever truly diverge, add a small
 * {@code channelExtras} map here rather than subclassing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundGetReqDTO {

    /** External order number (the paid order's winning extension {@code no}). */
    @NotEmpty(message = "outTradeNo cannot be empty")
    private String outTradeNo;

    /** External refund number ({@code PayRefundPO.no}). */
    @NotEmpty(message = "outRefundNo cannot be empty")
    private String outRefundNo;

    /** Original order create time; some rails (VNPay) require it to locate the transaction. */
    private Instant createTime;

    /** Channel-side transaction number of the original payment. Optional. */
    private String channelOrderNo;

}
