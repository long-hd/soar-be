package com.hdl.soar.module.system.mapper.permission;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RoleRespDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RoleSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import com.hdl.soar.module.system.mapper.SystemEnumMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class, SystemEnumMapper.class})
public interface RoleMapper {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    RolePO toPO(RoleSaveReqDTO dto);
    void updatePO(RoleSaveReqDTO dto, @MappingTarget RolePO po);

    RoleRespDTO toDTO(RolePO po);
    List<RoleRespDTO> toDTOList(List<RolePO> poList);


}
