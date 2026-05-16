package com.hdl.soar.framework.common.biz.infra.logger;


import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import jakarta.validation.Valid;
import org.springframework.scheduling.annotation.Async;

/**
 * API error log API interface
 */
public interface ApiErrorLogCommonApi {

    /**
     * Create API error log
     *
     * @param createDTO creation information
     */
    void createApiErrorLog(@Valid ApiErrorLogCreateReqDTO createDTO);

    /**
     * Asynchronous creation of API error log
     *
     * @param createDTO error log DTO
     */
    @Async
    default void createApiErrorLogAsync(ApiErrorLogCreateReqDTO createDTO) {
        createApiErrorLog(createDTO);
    }

}
