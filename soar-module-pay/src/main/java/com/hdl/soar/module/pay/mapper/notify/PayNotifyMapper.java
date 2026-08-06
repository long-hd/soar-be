package com.hdl.soar.module.pay.mapper.notify;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.pay.controller.admin.notify.dto.PayNotifyLogRespDTO;
import com.hdl.soar.module.pay.controller.admin.notify.dto.PayNotifyTaskRespDTO;
import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyLogPO;
import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyTaskPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface PayNotifyMapper {

    PayNotifyMapper INSTANCE = Mappers.getMapper(PayNotifyMapper.class);

    PayNotifyTaskRespDTO toDTO(PayNotifyTaskPO po);

    List<PayNotifyTaskRespDTO> toDTOList(List<PayNotifyTaskPO> poList);

    PayNotifyLogRespDTO toLogDTO(PayNotifyLogPO po);

    List<PayNotifyLogRespDTO> toLogDTOList(List<PayNotifyLogPO> poList);

}
