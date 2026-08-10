package com.hdl.soar.module.pay.framework.pay.core.client;

import com.hdl.soar.module.pay.framework.pay.core.client.dto.PayOrderGetReqDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderChannelRespDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;

import java.util.Map;

/**
 * A payment client: one live instance bound to one channel row, wrapping one rail (VNPay, mock, ...).
 * <p>
 * Trimmed to the order lifecycle for now; refund and transfer are added in a later slice.
 *
 * @param <Config> the channel's configuration type
 */
public interface PayClient<Config extends PayClientConfig> {

    /** The channel id this client is bound to. */
    Long getId();

    /** This client's configuration. */
    Config getConfig();

    /**
     * Start a payment on the rail.
     *
     * @param reqDTO unified order request
     * @return the channel result — {@code WAITING} with a redirect URL for async rails (VNPay),
     *         or {@code SUCCESS} immediately for synchronous ones (mock)
     */
    PayOrderChannelRespDTO unifiedOrder(PayOrderUnifiedReqDTO reqDTO);

    /**
     * Parse and verify a callback from the rail.
     *
     * @param params  query/form parameters
     * @param body    raw request body
     * @param headers request headers
     * @return the channel result
     */
    PayOrderChannelRespDTO parseOrderNotify(Map<String, String> params, String body, Map<String, String> headers);

    /**
     * Query the rail for an order's current status (used for reconciliation).
     *
     * @param reqDTO request to query a transaction's status at the channel
     * @return the channel result
     */
    PayOrderChannelRespDTO getOrder(PayOrderGetReqDTO reqDTO);

}
