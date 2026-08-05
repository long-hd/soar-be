package com.hdl.soar.module.pay.service.channel;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.controller.admin.channel.dto.PayChannelPageReqDTO;
import com.hdl.soar.module.pay.controller.admin.channel.dto.PayChannelSaveReqDTO;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClient;

/**
 * Payment channel service.
 */
public interface PayChannelService {

    Long createChannel(PayChannelSaveReqDTO createReqDTO);

    void updateChannel(PayChannelSaveReqDTO updateReqDTO);

    void deleteChannel(Long id);

    PayChannelPO getChannel(Long id);

    PageResult<PayChannelPO> getChannelPage(PayChannelPageReqDTO pageReqDTO);

    /**
     * Load an enabled channel by app and code.
     *
     * @param appId app id
     * @param code  channel code
     * @return the enabled channel
     */
    PayChannelPO validChannel(Long appId, String code);

    /**
     * Load an enabled channel by id.
     *
     * @param id channel id
     * @return the enabled channel
     */
    PayChannelPO validChannel(Long id);

    /**
     * Resolve (build or refresh) and return the live {@code PayClient} for a channel.
     *
     * @param channelId channel id
     * @return the client bound to that channel
     */
    PayClient<?> getPayClient(Long channelId);

}
