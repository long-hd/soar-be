package com.hdl.soar.module.pay.service.order;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hdl.soar.module.pay.controller.admin.order.dto.PayOrderPageReqDTO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderExtensionPO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderChannelRespDTO;

public interface PayOrderService {

    /**
     * Create a payment order (idempotent per app + merchant order id).
     *
     * @param reqDTO create request
     * @return the order id
     */
    Long createOrder(PayOrderCreateReqDTO reqDTO);

    /**
     * Create a WAITING order extension for a channel attempt.
     * <p>
     * This is the channel-independent half of order submission: it validates the order and channel and
     * inserts a WAITING attempt with a freshly generated no. Slice 2 wraps this with the actual
     * {@code PayClient} call.
     *
     * @param orderId     order id
     * @param channelCode channel code
     * @param userIp      client ip
     * @return the created extension
     */
    PayOrderExtensionPO createOrderExtension(Long orderId, String channelCode, String userIp);

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

    PageResult<PayOrderPO> getOrderPage(PayOrderPageReqDTO pageReqDTO);

    PayOrderExtensionPO getOrderExtensionByNo(String no);

}
