package com.hdl.soar.module.system.mapper.dict;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataPageReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataRespDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictDataPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface DictDataMapper {
    DictDataMapper INSTANCE = Mappers.getMapper(DictDataMapper.class);

    // --- Create: DTO -> PO ---
    DictDataPO toPO(DictDataSaveReqDTO dto);

    // --- Update: DTO fields -> existing PO ---
    void updateDO(DictDataSaveReqDTO dto, @MappingTarget DictDataPO po);

    // --- Response: PO -> DTO ---
    DictDataRespDTO toDTO(DictDataPO po);
    List<DictDataRespDTO> toDTOList(List<DictDataPO> poList);

    // Simple list: PO -> SimpleDTO
    DictDataSimpleRespDTO toSimpleDTO(DictDataPO po);
    List<DictDataSimpleRespDTO> toSimpleDTOList(List<DictDataPO> list);
}
