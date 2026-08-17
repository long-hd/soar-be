package com.hdl.soar.module.pay.service.order;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hdl.soar.module.pay.controller.admin.order.dto.PayOrderPageReqDTO;
import com.hdl.soar.module.pay.controller.app.order.dto.PayOrderSubmitReqDTO;
import com.hdl.soar.module.pay.controller.app.order.dto.PayOrderSubmitRespDTO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderExtensionPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderChannelRespDTO;

import java.math.BigDecimal;

public interface PayOrderService {

    /**
     * Create a payment order (idempotent per app + merchant order id).
     *
     * @param reqDTO create request
     * @return the order id
     */
    Long createOrder(PayOrderCreateReqDTO reqDTO);

    /**
     * Handle a channel notify/result and advance the order state machine.
     * <p>
     * Resolves the channel, restores its tenant context, then delegates to the transactional handler.
     *
     * @param channelId channel id the notify arrived for
     * @param notify    channel result
     */
    void notifyOrder(Long channelId, PayOrderChannelRespDTO notify);

    PayOrderPO getOrder(Long id);

    /** Look up a paid order by app + merchant order id (used by the refund flow). */
    PayOrderPO getOrder(Long appId, String merchantOrderId);

    /** Add {@code delta} to the order's running refund total and flip it to REFUND (atomic). */
    void updateOrderRefundPrice(Long orderId, BigDecimal delta);

    PageResult<PayOrderPO> getOrderPage(PayOrderPageReqDTO pageReqDTO);

    PayOrderExtensionPO getOrderExtensionByNo(String no);

    /**
     * Submit an order to a channel: create the attempt, call the rail, and (for synchronous rails)
     * advance the state machine. Not transactional — a failed rail call must leave the attempt on record.
     *
     * @param reqDTO submit request (order id + channel code)
     * @param userIp client ip
     * @return how to continue payment (redirect URL) or the already-paid status
     */
    PayOrderSubmitRespDTO submitOrder(PayOrderSubmitReqDTO reqDTO, String userIp);

    /**
     * Reconcile: for recent WAITING attempts, ask the channel whether payment actually landed and, if
     * so, drive the order to SUCCESS through {@link #notifyOrder}. Returns the number of attempts that
     * recovered. Called by the sync job.
     */
    int syncOrder();

    /**
     * Expire: for WAITING orders past their expire time, re-check each attempt against the channel
     * (recover if paid) and otherwise close the order. Returns the number of orders closed. Called by
     * the expire job.
     */
    int expireOrder();

}
