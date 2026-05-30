package com.hdl.soar.module.system.mapper.dept;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptRespDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface DeptMapper {
    DeptMapper INSTANCE = Mappers.getMapper(DeptMapper.class);

    DeptPO toPO(DeptSaveReqDTO dto);
    void updatePO(DeptSaveReqDTO dto, @MappingTarget DeptPO po);

    DeptRespDTO toDTO(DeptPO po);
    List<DeptRespDTO> toDTOList(List<DeptPO> list);

    DeptSimpleRespDTO toSimpleDTO(DeptPO po);
    List<DeptSimpleRespDTO> toSimpleDTOList(List<DeptPO> list);

}
