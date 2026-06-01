package com.hdl.soar.framework.common.biz.system.logger;

import com.hdl.soar.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import jakarta.validation.Valid;
import org.springframework.scheduling.annotation.Async;

/**
 * Operate log API interface.
 * <p>
 * Lives in soar-common so the framework layer (aspect) can call it
 * without depending on module-system.
 */
public interface OperateLogCommonApi {

    void createOperateLog(@Valid OperateLogCreateReqDTO createDTO);

    @Async
    default void createOperateLogAsync(OperateLogCreateReqDTO createDTO) {
        createOperateLog(createDTO);
    }

}
