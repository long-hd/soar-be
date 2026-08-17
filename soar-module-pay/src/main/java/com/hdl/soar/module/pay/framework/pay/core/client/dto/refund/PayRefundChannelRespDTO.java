package com.hdl.soar.module.pay.framework.pay.core.client.dto.refund;

import com.hdl.soar.module.pay.enums.PayRefundStatusEnum;
import lombok.Data;

import java.time.Instant;

/**
 * Channel refund result. Built by a {@code PayClient} from a refund call, a refund callback, or a
 * refund status query, and fed into {@code PayRefundService.notifyRefund}.
 */
@Data
public class PayRefundChannelRespDTO {

    /** Status ({@link com.hdl.soar.module.pay.enums.PayRefundStatusEnum}). */
    private Integer status;

    /** External refund number ({@code PayRefundPO.no}). */
    private String outRefundNo;

    /** Channel-side refund number. */
    private String channelRefundNo;

    /** Time the refund succeeded. */
    private Instant successTime;

    /** Raw channel payload, kept for audit. */
    private Object rawData;

    /** Channel business error code (set on FAILURE; system errors throw instead). */
    private String channelErrorCode;

    /** Channel business error message (set on FAILURE). */
    private String channelErrorMsg;

    private PayRefundChannelRespDTO() {
    }

    /** WAITING: the channel accepted the request but the result is not yet known. */
    public static PayRefundChannelRespDTO waitingOf(String channelRefundNo,
                                                    String outRefundNo, Object rawData) {
        PayRefundChannelRespDTO dto = new PayRefundChannelRespDTO();
        dto.status = PayRefundStatusEnum.WAITING.getStatus();
        dto.channelRefundNo = channelRefundNo;
        dto.outRefundNo = outRefundNo;
        dto.rawData = rawData;
        return dto;
    }

    /** SUCCESS: the channel confirmed the money went back. */
    public static PayRefundChannelRespDTO successOf(String channelRefundNo, Instant successTime,
                                                    String outRefundNo, Object rawData) {
        PayRefundChannelRespDTO dto = new PayRefundChannelRespDTO();
        dto.status = PayRefundStatusEnum.SUCCESS.getStatus();
        dto.channelRefundNo = channelRefundNo;
        dto.successTime = successTime;
        dto.outRefundNo = outRefundNo;
        dto.rawData = rawData;
        return dto;
    }

    /** FAILURE: the channel rejected the refund (a business, not system, error). */
    public static PayRefundChannelRespDTO failureOf(String channelErrorCode, String channelErrorMsg,
                                                    String outRefundNo, Object rawData) {
        PayRefundChannelRespDTO dto = new PayRefundChannelRespDTO();
        dto.status = PayRefundStatusEnum.FAILURE.getStatus();
        dto.channelErrorCode = channelErrorCode;
        dto.channelErrorMsg = channelErrorMsg;
        dto.outRefundNo = outRefundNo;
        dto.rawData = rawData;
        return dto;
    }

}
