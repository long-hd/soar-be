package com.hdl.soar.module.pay.framework.pay.core.client.dto;

import lombok.Data;

import java.time.Instant;

/**
 * Request to query a transaction's status at the channel. Flat and rail-agnostic: each rail reads the
 * fields it needs and ignores the rest. Add a field here when a new rail needs one — no signature change.
 */
@Data
public class PayOrderGetReqDTO {

    /** Our per-attempt number (the extension's {@code no}) that was sent to the channel. */
    private String outTradeNo;

    /** When that attempt was created — some rails (VNPay querydr) require the original transaction date. */
    private Instant createTime;

}
