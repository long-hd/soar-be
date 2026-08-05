package com.hdl.soar.module.pay.mapper.order;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hdl.soar.module.pay.controller.admin.order.dto.PayOrderRespDTO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.mapper.PayEnumMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class, PayEnumMapper.class})
public interface PayOrderMapper {

    PayOrderMapper INSTANCE = Mappers.getMapper(PayOrderMapper.class);

    /**
     * Map a create request to a new order PO. The service fills app id, notify url, status, and the
     * initial refund amount; currency is mapped by name (e.g. "VND" -> VND) after validation.
     */
    PayOrderPO toPO(PayOrderCreateReqDTO dto);

    PayOrderRespDTO toDTO(PayOrderPO po);

    List<PayOrderRespDTO> toDTOList(List<PayOrderPO> poList);

}
