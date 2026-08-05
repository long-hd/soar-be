package com.hdl.soar.module.pay.framework.pay.core.client.dto.order;

import lombok.Data;

import java.time.Instant;

/**
 * Channel result / notify payload for a payment order.
 * <p>
 * Produced by a channel callback or by a {@code PayClient} query (slice 2), and consumed by the
 * order state machine. {@link #outTradeNo} matches back to a {@code PayOrderExtensionPO.no}.
 */
@Data
public class PayOrderChannelRespDTO {

    /**
     * Status, see {@code PayOrderStatusEnum}. Only SUCCESS and CLOSED are acted upon.
     */
    private Integer status;

    /** External order number — matches {@code PayOrderExtensionPO.no}. */
    private String outTradeNo;

    /** Channel-side order number. */
    private String channelOrderNo;

    /** Channel-side user id. */
    private String channelUserId;

    /** Time the payment succeeded. */
    private Instant successTime;

    /** Raw channel payload, stored for audit. */
    private String rawData;

    /** Channel error code, when failed. */
    private String channelErrorCode;

    /** Channel error message, when failed. */
    private String channelErrorMsg;

}
