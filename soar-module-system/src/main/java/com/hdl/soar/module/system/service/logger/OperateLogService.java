package com.hdl.soar.module.system.service.logger;

import com.hdl.soar.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.system.controller.admin.logger.dto.operatelog.OperateLogPageReqDTO;
import com.hdl.soar.module.system.dal.entity.logger.OperateLogPO;

/**
 * Service interface for operation logs.
 */
public interface OperateLogService {

    /**
     * Record an operation log.
     *
     * @param createDTO request for creating an operation log
     */
    void createOperateLog(OperateLogCreateReqDTO createDTO);

    /**
     * Get an operation log by ID.
     *
     * @param id log ID
     * @return operation log
     */
    OperateLogPO getOperateLog(Long id);

    /**
     * Get paginated list of operation logs.
     *
     * @param pageReqDTO pagination request
     * @return paginated operation log list
     */
    PageResult<OperateLogPO> getOperateLogPage(OperateLogPageReqDTO pageReqDTO);

}
