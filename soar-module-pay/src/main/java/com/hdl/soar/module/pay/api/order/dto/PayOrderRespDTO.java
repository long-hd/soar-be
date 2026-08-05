package com.hdl.soar.module.pay.api.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Order view returned to other modules via {@code PayOrderApi.getOrder}.
 */
@Data
public class PayOrderRespDTO {

    /** Order id. */
    private Long id;

    /** Owning app id. */
    private Long appId;

    /** Merchant order id. */
    private String merchantOrderId;

    /** Amount. */
    private BigDecimal price;

    /** Currency (ISO 4217 alpha). */
    private String currency;

    /** Status, see {@code PayOrderStatusEnum}. */
    private Integer status;

    /** Time paid, if paid. */
    private Instant successTime;

    /** Winning payment no. */
    private String no;

    /** Channel-side order number. */
    private String channelOrderNo;

}
