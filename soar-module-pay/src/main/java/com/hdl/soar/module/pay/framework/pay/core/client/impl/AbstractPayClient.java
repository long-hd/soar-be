package com.hdl.soar.module.pay.framework.pay.core.client.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClient;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClientConfig;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderChannelRespDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.exception.PayClientException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Base class for {@link PayClient} implementations.
 * <p>
 * Template method: the public {@code unifiedOrder} / {@code getOrder} / {@code parseOrderNotify} are
 * final and wrap the subclass's {@code doXxx} with uniform error handling, so every rail turns a raw
 * failure into a {@link PayClientException}. Subclasses implement {@link #doInit()} and the {@code doXxx}
 * methods. One instance is created per channel row and cached by the factory.
 *
 * @param <Config> the channel's configuration type
 */
@Slf4j
public abstract class AbstractPayClient<Config extends PayClientConfig> implements PayClient<Config> {

    private final Long channelId;

    protected Config config;

    protected AbstractPayClient(Long channelId, Config config) {
        this.channelId = channelId;
        this.config = config;
    }

    /** Validate the config and run rail-specific initialization. Called by the factory after creation. */
    public final void init() {
        config.validate(SpringUtil.getBean(Validator.class));
        doInit();
        log.debug("[init][client({}) initialized]", getId());
    }

    /** Rebuild the client when the channel config changes. */
    public final void refresh(Config config) {
        if (ObjectUtil.equal(config, this.config)) {
            return;
        }
        log.info("[refresh][client({}) config changed, re-initializing]", getId());
        this.config = config;
        this.init();
    }

    protected abstract void doInit();

    @Override
    public Long getId() {
        return channelId;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    // =================  Unified Order

    @Override
    public final PayOrderChannelRespDTO unifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        try {
            return doUnifiedOrder(reqDTO);
        } catch (Throwable ex) {
            throw buildException("unifiedOrder", reqDTO, ex);
        }
    }

    protected abstract PayOrderChannelRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Throwable;

    @Override
    public final PayOrderChannelRespDTO parseOrderNotify(Map<String, String> params, String body,
                                                         Map<String, String> headers) {
        try {
            return doParseOrderNotify(params, body, headers);
        } catch (Throwable ex) {
            throw buildException("parseOrderNotify", body, ex);
        }
    }

    protected abstract PayOrderChannelRespDTO doParseOrderNotify(Map<String, String> params, String body,
                                                                 Map<String, String> headers) throws Throwable;

    // ============= Get Order

    @Override
    public PayOrderChannelRespDTO getOrder(String outTradeNo) {
        try {
            return doGetOrder(outTradeNo);
        } catch (Throwable ex) {
            throw buildException("getOrder", outTradeNo, ex);
        }
    }

    protected abstract PayOrderChannelRespDTO doGetOrder(String outTradeNo) throws Throwable;

    // ============= helper

    private PayClientException buildException(String op, Object req, Throwable ex) {
        if (ex instanceof PayClientException) {
            return (PayClientException) ex;
        }
        log.error("[{}][client({}) request({}) failed]", op, getId(), req, ex);
        return new PayClientException(ex);
    }

}
