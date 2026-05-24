package com.hdl.soar.module.system.service.logger;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.hdl.soar.module.system.controller.admin.logger.dto.loginlog.LoginLogPageReqDTO;
import com.hdl.soar.module.system.dal.entity.logger.LoginLogPO;
import jakarta.validation.Valid;

/**
 * Login Log Service Interface
 */
public interface LoginLogService {

    /**
     * Get a login log
     *
     * @param id the ID
     * @return the login log
     */
    LoginLogPO getLoginLog(Long id);

    /**
     * Get paginated login logs
     *
     * @param pageReqDTO pagination conditions
     * @return paginated login logs
     */
    PageResult<LoginLogPO> getLoginLogPage(LoginLogPageReqDTO pageReqDTO);

    /**
     * Create a login log
     *
     * @param reqDTO log information
     */
    void createLoginLog(@Valid LoginLogCreateReqDTO reqDTO);

}
