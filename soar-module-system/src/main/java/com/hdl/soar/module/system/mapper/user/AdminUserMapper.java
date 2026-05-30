package com.hdl.soar.module.system.mapper.user;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserRespDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.mapper.SystemEnumMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class, SystemEnumMapper.class})
public interface AdminUserMapper {
    AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

    @Mapping(target = "password", ignore = true) // Password handled manually (encrypt)
    @Mapping(target = "status", ignore = true)   // Status hardcoded to ENABLE on create
    AdminUserPO toPO(UserSaveReqDTO dto);

    @Mapping(target = "password", ignore = true) // Never update password through this mapper
    void updatePO(UserSaveReqDTO dto, @MappingTarget AdminUserPO po);

    @Mapping(target = "deptName", ignore = true) // Populated in controller
    UserRespDTO toDTO(AdminUserPO po);
    List<UserRespDTO> toDTOList(List<AdminUserPO> list);

    @Mapping(target = "deptName", ignore = true)
    UserSimpleRespDTO toSimpleDTO(AdminUserPO po);
    List<UserSimpleRespDTO> toSimpleDTOList(List<AdminUserPO> list);

}
