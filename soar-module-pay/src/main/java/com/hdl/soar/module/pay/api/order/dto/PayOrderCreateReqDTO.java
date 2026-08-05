package com.hdl.soar.module.pay.api.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request to create a payment order. This is the published contract a business module calls with.
 */
@Data
public class PayOrderCreateReqDTO {

    /** App key identifying the calling business system. */
    @NotBlank(message = "App key cannot be empty")
    private String appKey;

    /** Merchant order id — unique per app. */
    @NotBlank(message = "Merchant order id cannot be empty")
    private String merchantOrderId;

    /** Product title. */
    @NotBlank(message = "Subject cannot be empty")
    private String subject;

    /** Product description. */
    private String body;

    /** Amount to collect; must be positive. */
    @NotNull(message = "Price cannot be empty")
    @DecimalMin(value = "0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    /** ISO 4217 alpha currency code (e.g. VND, USD). */
    @NotBlank(message = "Currency cannot be empty")
    private String currency;

    /** Order expiry time. */
    @NotNull(message = "Expire time cannot be empty")
    private Instant expireTime;

}
