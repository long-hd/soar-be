package com.hdl.soar.module.pay.api.notify;

import lombok.Data;

import java.io.Serializable;

/**
 * The body POSTed to the merchant's notify URL when an order is paid.
 * <p>
 * Deliberately minimal: it is a <b>signal</b> ("order X changed, go check"), not a source of truth.
 * Delivery is at-least-once and the endpoint is public, so a merchant must never settle money from
 * this body — it should re-query pay for the authoritative status keyed by {@link #merchantOrderId}.
 */
@Data
public class PayOrderNotifyReqDTO implements Serializable {

    /** The merchant's own order id. */
    private String merchantOrderId;

    /** The pay-side order id. */
    private Long payOrderId;

}
