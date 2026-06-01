package com.hdl.soar.module.system.mapper.logger;

import com.hdl.soar.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.system.controller.admin.logger.dto.operatelog.OperateLogRespDTO;
import com.hdl.soar.module.system.dal.entity.logger.OperateLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface OperateLogMapper {
    OperateLogMapper INSTANCE = Mappers.getMapper(OperateLogMapper.class);

    OperateLogPO toPO(OperateLogCreateReqDTO dto);

    OperateLogRespDTO toDTO(OperateLogPO po);
    List<OperateLogRespDTO> toDTOList(List<OperateLogPO> list);

}
