package com.hdl.soar.module.infra.api.logger;

import com.hdl.soar.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.tenant.core.util.TenantUtils;
import com.hdl.soar.module.infra.service.logger.ApiAccessLogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

/**
 * API access log API implementation class
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiAccessLogApiImpl implements ApiAccessLogCommonApi {

    ApiAccessLogService apiAccessLogService;

    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        if (TenantContextHolder.getTenantId() != null) {
            apiAccessLogService.createApiAccessLog(createDTO);
        } else {
            // Edge case: request arrives before tenant context is resolved
            // (e.g., invalid tenant header, health check endpoint)
            TenantUtils.executeIgnore(() -> apiAccessLogService.createApiAccessLog(createDTO));
        }
    }
}
