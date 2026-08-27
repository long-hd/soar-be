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


    /** Prefix for generated refund numbers. */
    private String refundNoPrefix = "R";

    /**
     * Base URL a channel should call back to with a refund result (channel id appended). Optional:
     * synchronous rails (VNPay) never call back. When null, no refund notify URL is sent to the rail.
     */
    private String refundNotifyUrl;

    /** Only WAITING refunds created within this window are reconciled by the refund sync job. */
    private Duration refundSyncCreateTimeWithin = Duration.ofMinutes(10);

    /**
     * Enables the MOCK payment rail and the {@code /pay/test/*} controller. A dev/test switch:
     * the MOCK rail settles orders without a real gateway, so it must never be reachable in
     * production. Default {@code false}; set {@code true} only in local/dev profiles. Mirrors
     * {@code soar.security.mock-enable}.
     */
    private Boolean mockEnable = false;

}
