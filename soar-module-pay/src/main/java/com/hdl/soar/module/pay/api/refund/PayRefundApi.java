package com.hdl.soar.module.pay.api.refund;

import com.hdl.soar.module.pay.api.refund.dto.PayRefundCreateReqDTO;

/**
 * In-process refund API. Same in-process-only posture as {@code PayOrderApi}: no REST endpoint yet
 * (seed via test/scratch controller); an external merchant would go through a future HMAC gate.
 */
public interface PayRefundApi {

    /**
     * Create and initiate a refund. Idempotent on {@code merchantRefundId}.
     *
     * @return the created refund id
     */
    Long createRefund(PayRefundCreateReqDTO reqDTO);

}
