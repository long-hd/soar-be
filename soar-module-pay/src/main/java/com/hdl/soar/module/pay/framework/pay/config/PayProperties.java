package com.hdl.soar.module.pay.framework.pay.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Pay module configuration, bound from {@code soar.pay.*}.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "soar.pay")
public class PayProperties {

    /**
     * Base URL a channel should call back to on payment completion. The channel id is appended per
     * request, giving e.g. {@code https://host/app-api/pay/notify/order/{channelId}}.
     * <p>
     * Environment-specific (localhost in dev, the public domain in prod), so it must come from config.
     */
    @NotEmpty(message = "Order notify URL cannot be empty")
    private String orderNotifyUrl;

    /**
     * Prefix for generated payment numbers.
     */
    private String orderNoPrefix = "P";

    /**
     * Only WAITING orders created within this window are reconciled by the sync job. A payment
     * usually resolves within ~10 min, so polling a recent window keeps querydr volume bounded;
     * older WAITING orders are left to the expire job.
     */
    private Duration orderSyncCreateTimeWithin = Duration.ofMinutes(10);

}
