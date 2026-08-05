package com.hdl.soar.module.pay.framework.pay.core.client.dto.order;

import com.hdl.soar.module.pay.enums.PayCurrencyEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Request handed to a {@code PayClient} to start a payment on its rail.
 * <p>
 * Built by the order service from an order plus its extension. {@link #outTradeNo} is the extension's
 * {@code no}; the rail echoes it back in the callback so the result can be matched.
 */
@Data
public class PayOrderUnifiedReqDTO {

    /** Client IP. */
    private String userIp;

    /** External order number — the extension's {@code no}. */
    private String outTradeNo;

    /** Product title. */
    private String subject;

    /** Product description. */
    private String body;

    /** Amount to collect. */
    private BigDecimal price;

    /** Settlement currency. */
    private PayCurrencyEnum currency;

    /** URL the rail should call back on completion (points at this gateway). */
    private String notifyUrl;

    /** URL the rail should redirect the user back to after payment. */
    private String returnUrl;

    /** Order expiry time. */
    private Instant expireTime;

    /** Extra channel-specific parameters. */
    private Map<String, String> channelExtras;

}
