package com.hdl.soar.module.pay.framework.pay.core.client;

/**
 * Builds and caches one {@link PayClient} per channel row.
 * <p>
 * Mirrors the infra file-client factory: {@code getPayClient} returns a cached instance;
 * {@code createOrUpdatePayClient} builds it on first use (or refreshes it when the config changes).
 */
public interface PayClientFactory {

    /**
     * Get the cached client for a channel, or {@code null} if it has not been created yet.
     */
    PayClient<?> getPayClient(Long channelId);

    /**
     * Create the client for a channel, or refresh it if it already exists.
     *
     * @param channelId   channel id
     * @param channelCode channel code — resolves the client class via {@code PayChannelEnum}
     * @param config      the channel's typed configuration
     */
    <Config extends PayClientConfig> void createOrUpdatePayClient(Long channelId, String channelCode, Config config);

}
