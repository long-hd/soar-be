package com.hdl.soar.module.pay.framework.pay.core.client.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.hdl.soar.module.pay.enums.PayChannelEnum;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClient;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClientConfig;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClientFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default factory. Holds one live client per channel id. The client class for a code is looked up on
 * {@link PayChannelEnum} (the rail descriptor) and instantiated by reflection with {@code (channelId, config)}.
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayClientFactoryImpl implements PayClientFactory {

    ConcurrentMap<Long, AbstractPayClient<?>> clients = new ConcurrentHashMap<>();

    @Override
    public PayClient<?> getPayClient(Long channelId) {
        AbstractPayClient<?> client = clients.get(channelId);
        if (client == null) {
            log.error("[getPayClient][channel({}) has no client]", channelId);
        }
        return client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Config extends PayClientConfig> void createOrUpdatePayClient(Long channelId, String channelCode, Config config) {
        PayChannelEnum channelEnum = PayChannelEnum.of(channelCode);
        Assert.notNull(channelEnum, "Payment channel ({}) is not supported", channelCode);
        AbstractPayClient<Config> client = (AbstractPayClient<Config>) clients.get(channelId);
        // Rebuild (not just refresh) when the cached client is a different rail than the channel's
        // current code: a code change makes the old client the wrong class, and refreshing it with
        // the new config would silently run the wrong rail.
        if (client == null || client.getClass() != channelEnum.getClientClass()) {
            client = createPayClient(channelEnum, channelId, config);
            client.init();
            clients.put(channelId, client);
        } else {
            client.refresh(config);
        }
    }

    @SuppressWarnings("unchecked")
    private <Config extends PayClientConfig> AbstractPayClient<Config> createPayClient(
            PayChannelEnum channelEnum, Long channelId, Config config) {
        return (AbstractPayClient<Config>) ReflectUtil.newInstance(channelEnum.getClientClass(), channelId, config);
    }

    @Override
    public void removePayClient(Long channelId) {
        AbstractPayClient<?> removed = clients.remove(channelId);
        if (removed != null) {
            log.info("[removePayClient][channel({}) client evicted]", channelId);
        }
    }

}
