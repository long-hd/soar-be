package com.hdl.soar.module.system.mapper.logger;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.hdl.soar.module.system.controller.admin.logger.dto.loginlog.LoginLogRespDTO;
import com.hdl.soar.module.system.dal.entity.logger.LoginLogPO;
import com.hdl.soar.module.system.mapper.SystemEnumMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class, SystemEnumMapper.class})
public interface LoginLogMapper {
    LoginLogMapper INSTANCE = Mappers.getMapper(LoginLogMapper.class);

    // --- Response: PO -> DTO ---
    LoginLogRespDTO toDTO(LoginLogPO po);
    List<LoginLogRespDTO> toDTOList(List<LoginLogPO> list);

    // --- Create: API DTO -> PO ---
    LoginLogPO toPO(LoginLogCreateReqDTO dto);

}
