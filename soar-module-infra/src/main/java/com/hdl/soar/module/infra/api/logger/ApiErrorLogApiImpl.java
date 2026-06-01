package com.hdl.soar.module.infra.api.logger;

import com.hdl.soar.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.tenant.core.util.TenantUtils;
import com.hdl.soar.module.infra.service.logger.ApiErrorLogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

/**
 * API access log API interface
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiErrorLogApiImpl implements ApiErrorLogCommonApi {

    ApiErrorLogService apiErrorLogService;

    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {
        if (TenantContextHolder.getTenantId() != null) {
            apiErrorLogService.createApiErrorLog(createDTO);
        } else {
            // Edge case: request arrives before tenant context is resolved
            // (e.g., invalid tenant header, health check endpoint)
            TenantUtils.executeIgnore(() -> apiErrorLogService.createApiErrorLog(createDTO));
        }
    }
}
