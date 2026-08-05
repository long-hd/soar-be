package com.hdl.soar.module.pay.api.order;

import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import jakarta.validation.Valid;

/**
 * Order API for other modules.
 * <p>
 * Kept as a package inside the pay module for now; extract to a dedicated {@code -api} module once a
 * real consumer exists.
 */
public interface PayOrderApi {

    /**
     * Create a payment order.
     *
     * @param reqDTO create request
     * @return the order id
     */
    Long createOrder(@Valid PayOrderCreateReqDTO reqDTO);

}
