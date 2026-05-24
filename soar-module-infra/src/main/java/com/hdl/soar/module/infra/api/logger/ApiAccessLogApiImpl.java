package com.hdl.soar.module.infra.api.logger;

import com.hdl.soar.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import org.springframework.stereotype.Service;

/**
 * API access log API implementation class
 */
@Service
public class ApiAccessLogApiImpl implements ApiAccessLogCommonApi {
    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        throw new UnsupportedOperationException("Not implements yet.");
    }
}
