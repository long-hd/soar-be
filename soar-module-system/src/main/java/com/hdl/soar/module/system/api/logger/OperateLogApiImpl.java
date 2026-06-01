package com.hdl.soar.module.system.api.logger;

import com.hdl.soar.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.hdl.soar.module.system.service.logger.OperateLogService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class OperateLogApiImpl implements OperateLogApi {

    OperateLogService operateLogService;

    @Override
    public void createOperateLog(OperateLogCreateReqDTO createDTO) {
        operateLogService.createOperateLog(createDTO);
    }

}
