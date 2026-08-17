package com.hdl.soar.module.pay.mapper.refund;

import com.hdl.soar.module.pay.controller.admin.refund.dto.PayRefundRespDTO;
import com.hdl.soar.module.pay.dal.entity.refund.PayRefundPO;
import com.hdl.soar.module.pay.mapper.PayEnumMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct mapper for the refund admin view. Uses {@link PayEnumMapper} for the status enum ->
 * Integer conversion (per-enum method; add {@code toInt(PayRefundStatusEnum)} there).
 */
@Mapper(uses = PayEnumMapper.class)
public interface PayRefundMapper {

    PayRefundMapper INSTANCE = Mappers.getMapper(PayRefundMapper.class);

    PayRefundRespDTO toDTO(PayRefundPO po);

    List<PayRefundRespDTO> toDTOList(List<PayRefundPO> pos);

}
