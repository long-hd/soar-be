package com.hdl.soar.framework.common.biz.infra.logger;

import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import jakarta.validation.Valid;
import org.springframework.scheduling.annotation.Async;


/**
 * API access log API interface
 */
public interface ApiAccessLogCommonApi {

    /**
     * Create API access log
     *
     * @param createDTO creation information
     */
    void createApiAccessLog(@Valid ApiAccessLogCreateReqDTO createDTO);

    /**
     * Asynchronous creation of API access log
     *
     * @param createDTO access log DTO
     */
    @Async
    default void createApiAccessLogAsync(ApiAccessLogCreateReqDTO createDTO) {
        createApiAccessLog(createDTO);
    }

}
