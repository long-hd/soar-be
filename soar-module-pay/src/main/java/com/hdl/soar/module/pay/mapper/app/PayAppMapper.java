package com.hdl.soar.module.pay.mapper.app;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.pay.controller.admin.app.dto.PayAppRespDTO;
import com.hdl.soar.module.pay.controller.admin.app.dto.PayAppSaveReqDTO;
import com.hdl.soar.module.pay.dal.entity.app.PayAppPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface PayAppMapper {

    PayAppMapper INSTANCE = Mappers.getMapper(PayAppMapper.class);

    PayAppPO toPO(PayAppSaveReqDTO dto);

    void updatePO(PayAppSaveReqDTO dto, @MappingTarget PayAppPO po);

    PayAppRespDTO toDTO(PayAppPO po);

    List<PayAppRespDTO> toDTOList(List<PayAppPO> poList);

}
