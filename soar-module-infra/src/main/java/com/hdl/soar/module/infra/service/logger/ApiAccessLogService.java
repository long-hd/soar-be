package com.hdl.soar.module.infra.service.logger;

import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.infra.controller.admin.logger.dto.apiaccesslog.ApiAccessLogPageReqDTO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiAccessLogPO;

/**
 * Service interface for API access logs.
 */
public interface ApiAccessLogService {

    /**
     * Creates an API access log.
     *
     * @param createDTO the API access log creation request
     */
    void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO);

    /**
     * Retrieves an API access log by its ID.
     *
     * @param id the log ID
     * @return the API access log
     */
    ApiAccessLogPO getApiAccessLog(Long id);

    /**
     * Retrieves a paginated list of API access logs.
     *
     * @param pageReqDTO the pagination query request
     * @return a page of API access logs
     */
    PageResult<ApiAccessLogPO> getApiAccessLogPage(ApiAccessLogPageReqDTO pageReqDTO);

    /**
     * Cleans API access logs older than the specified number of days.
     *
     * @param exceedDay the retention period in days; logs older than this value will be deleted
     * @param deleteLimit the maximum number of logs to delete in a single cleanup operation
     * @return the number of deleted logs
     */
    // TODO: implement when Job module is ready
    Integer cleanAccessLog(Integer exceedDay, Integer deleteLimit);

}
