package com.hdl.soar.module.pay.service.app;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.controller.admin.app.dto.PayAppPageReqDTO;
import com.hdl.soar.module.pay.controller.admin.app.dto.PayAppSaveReqDTO;
import com.hdl.soar.module.pay.dal.entity.app.PayAppPO;

/**
 * Payment app service.
 */
public interface PayAppService {

    Long createApp(PayAppSaveReqDTO createReqDTO);

    void updateApp(PayAppSaveReqDTO updateReqDTO);

    void deleteApp(Long id);

    PayAppPO getApp(Long id);

    PageResult<PayAppPO> getAppPage(PayAppPageReqDTO pageReqDTO);

    /**
     * Load an app by id and assert it exists and is enabled.
     *
     * @param id app id
     * @return the enabled app
     */
    PayAppPO validApp(Long id);

    /**
     * Load an app by key and assert it exists and is enabled.
     *
     * @param appKey app key
     * @return the enabled app
     */
    PayAppPO validApp(String appKey);

}
