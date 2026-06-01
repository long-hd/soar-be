package com.hdl.soar.module.infra.mapper.logger;

import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.infra.controller.admin.logger.dto.apierrorlog.ApiErrorLogRespDTO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiErrorLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface ApiErrorLogMapper {
    ApiErrorLogMapper INSTANCE = Mappers.getMapper(ApiErrorLogMapper.class);

    ApiErrorLogPO toPO(ApiErrorLogCreateReqDTO dto);

    ApiErrorLogRespDTO toDTO(ApiErrorLogPO po);
    List<ApiErrorLogRespDTO> toDTOList(List<ApiErrorLogPO> list);

}
