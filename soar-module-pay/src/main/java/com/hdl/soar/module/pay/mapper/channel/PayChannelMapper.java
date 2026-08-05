package com.hdl.soar.module.pay.mapper.channel;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.pay.controller.admin.channel.dto.PayChannelRespDTO;
import com.hdl.soar.module.pay.controller.admin.channel.dto.PayChannelSaveReqDTO;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface PayChannelMapper {

    PayChannelMapper INSTANCE = Mappers.getMapper(PayChannelMapper.class);

    PayChannelPO toPO(PayChannelSaveReqDTO dto);

    void updatePO(PayChannelSaveReqDTO dto, @MappingTarget PayChannelPO po);

    PayChannelRespDTO toDTO(PayChannelPO po);

    List<PayChannelRespDTO> toDTOList(List<PayChannelPO> poList);

}
