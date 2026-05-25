package com.hdl.soar.module.infra.api.logger;

import com.hdl.soar.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import org.springframework.stereotype.Service;

/**
 * API access log API interface
 */
@Service
public class ApiErrorLogApiImpl implements ApiErrorLogCommonApi {
    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {

    }
}
