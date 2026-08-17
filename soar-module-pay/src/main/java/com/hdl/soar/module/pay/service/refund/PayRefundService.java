package com.hdl.soar.module.pay.service.refund;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import com.hdl.soar.module.pay.controller.admin.refund.dto.PayRefundPageReqDTO;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO;
import com.hdl.soar.module.pay.dal.entity.refund.PayRefundPO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.refund.PayRefundChannelRespDTO;

public interface PayRefundService {

    /** Create and initiate a refund. Idempotent on {@code merchantRefundId}. Returns the refund id. */
    Long createRefund(PayRefundCreateReqDTO reqDTO);

    /**
     * Apply a channel refund result to our refund (from an inline result, a callback, or a query).
     * Resolves the channel's tenant, then runs the transactional handler in that tenant.
     */
    void notifyRefund(Long channelId, PayRefundChannelRespDTO notify);

    /**
     * Transactional handler — public only so {@code getSelf()} can call it through the proxy.
     * Do not call directly from another bean.
     */
    void notifyRefundInTransaction(PayChannelPO channel, PayRefundChannelRespDTO notify);

    /** Reconcile WAITING refunds by re-querying the channel. Returns the number resolved. */
    int syncRefund();

    // ---- query (admin) ----

    PayRefundPO getRefund(Long id);

    PageResult<PayRefundPO> getRefundPage(PayRefundPageReqDTO pageReqDTO);

}
