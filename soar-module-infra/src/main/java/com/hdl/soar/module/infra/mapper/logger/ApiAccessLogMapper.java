package com.hdl.soar.module.infra.mapper.logger;

import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.infra.controller.admin.logger.dto.ApiAccessLogRespDTO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiAccessLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface ApiAccessLogMapper {
    ApiAccessLogMapper INSTANCE = Mappers.getMapper(ApiAccessLogMapper.class);

    ApiAccessLogPO toPO(ApiAccessLogCreateReqDTO dto);

    ApiAccessLogRespDTO toDTO(ApiAccessLogPO po);
    List<ApiAccessLogRespDTO> toDTOList(List<ApiAccessLogPO> list);
}
