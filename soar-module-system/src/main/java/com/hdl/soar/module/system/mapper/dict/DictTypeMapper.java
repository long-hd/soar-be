package com.hdl.soar.module.system.mapper.dict;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeRespDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictTypePO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface DictTypeMapper {
    DictTypeMapper INSTANCE = Mappers.getMapper(DictTypeMapper.class);

    // --- Create: DTO -> PO ---
    DictTypePO toPO(DictTypeSaveReqDTO dto);

    // --- Update: DTO fields -> existing PO ---
    void updatePO(DictTypeSaveReqDTO dto, @MappingTarget DictTypePO po);

    // --- Response: PO -> DTO ---
    DictTypeRespDTO toDTO(DictTypePO po);
    // Page: PO list -> DTO list (MapStruct auto-generates loop)
    List<DictTypeRespDTO> toDTOList(List<DictTypePO> list);

    // Simple list: PO -> SimpleDTO
    DictTypeSimpleRespDTO toSimpleDTO(DictTypePO po);
    List<DictTypeSimpleRespDTO> toSimpleDTOList(List<DictTypePO> list);

}
