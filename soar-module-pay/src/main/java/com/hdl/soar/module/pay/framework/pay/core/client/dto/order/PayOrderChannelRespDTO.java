package com.hdl.soar.module.pay.framework.pay.core.client.dto.order;

import com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum;
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

    /** How the caller should present the next step (e.g. {@code url}), for WAITING results. */
    private String displayMode;

    /** The content to present (e.g. a redirect URL), for WAITING results. */
    private String displayContent;

    /** Raw channel payload, stored for audit. */
    private String rawData;

    /** Channel error code, when failed. */
    private String channelErrorCode;

    /** Channel error message, when failed. */
    private String channelErrorMsg;

    /** Build a WAITING result carrying how to continue payment (e.g. a redirect URL). */
    public static PayOrderChannelRespDTO waitingOf(String displayMode, String displayContent,
                                                   String outTradeNo, String rawData) {
        PayOrderChannelRespDTO resp = new PayOrderChannelRespDTO();
        resp.status = PayOrderStatusEnum.WAITING.getStatus();
        resp.displayMode = displayMode;
        resp.displayContent = displayContent;
        resp.outTradeNo = outTradeNo;
        resp.rawData = rawData;
        return resp;
    }

    /** Build a SUCCESS result. */
    public static PayOrderChannelRespDTO successOf(String channelOrderNo, String channelUserId,
                                                   Instant successTime, String outTradeNo, String rawData) {
        PayOrderChannelRespDTO resp = new PayOrderChannelRespDTO();
        resp.status = PayOrderStatusEnum.SUCCESS.getStatus();
        resp.channelOrderNo = channelOrderNo;
        resp.channelUserId = channelUserId;
        resp.successTime = successTime;
        resp.outTradeNo = outTradeNo;
        resp.rawData = rawData;
        return resp;
    }

    /** Build a CLOSED (failed/cancelled) result. */
    public static PayOrderChannelRespDTO closedOf(String channelErrorCode, String channelErrorMsg,
                                                  String outTradeNo, String rawData) {
        PayOrderChannelRespDTO resp = new PayOrderChannelRespDTO();
        resp.status = PayOrderStatusEnum.CLOSED.getStatus();
        resp.channelErrorCode = channelErrorCode;
        resp.channelErrorMsg = channelErrorMsg;
        resp.outTradeNo = outTradeNo;
        resp.rawData = rawData;
        return resp;
    }


}
