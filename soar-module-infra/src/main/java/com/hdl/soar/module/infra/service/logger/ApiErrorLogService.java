package com.hdl.soar.module.infra.service.logger;

import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.infra.controller.admin.logger.dto.apierrorlog.ApiErrorLogPageReqDTO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiErrorLogPO;

public interface ApiErrorLogService {

    /**
     * Create API error log.
     *
     * @param createDTO API error log data
     */
    void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO);

    /**
     * Get API error log by ID.
     *
     * @param id log ID
     * @return the error log, or null if not found
     */
    ApiErrorLogPO getApiErrorLog(Long id);

    /**
     * Get API error log page.
     *
     * @param pageReqDTO filter + pagination
     * @return paged results
     */
    PageResult<ApiErrorLogPO> getApiErrorLogPage(ApiErrorLogPageReqDTO pageReqDTO);

    /**
     * Update processing status of an error log.
     *
     * @param id            log ID
     * @param processStatus new processing status value
     * @param processUserId ID of the user performing the action
     */
    void updateApiErrorLogProcess(Long id, Integer processStatus, Long processUserId);

    /**
     * Clean error logs older than the specified number of days.
     *
     * @param exceedDay   number of days after which logs will be cleaned
     * @param deleteLimit number of records to delete per batch
     * @return total deleted rows
     */
    // TODO: implement when Job module is ready
    Integer cleanErrorLog(Integer exceedDay, Integer deleteLimit);


}
