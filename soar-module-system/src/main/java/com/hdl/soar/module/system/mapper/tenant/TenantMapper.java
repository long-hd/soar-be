package com.hdl.soar.module.system.mapper.tenant;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.system.controller.admin.tenant.dto.tenant.TenantRespDTO;
import com.hdl.soar.module.system.controller.admin.tenant.dto.tenant.TenantSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.tenant.TenantPO;
import com.hdl.soar.module.system.mapper.SystemEnumMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {EnumMapper.class, SystemEnumMapper.class})
public interface TenantMapper {
    TenantMapper INSTANCE = Mappers.getMapper(TenantMapper.class);

    TenantRespDTO toDTO(TenantPO po);

    TenantSimpleRespDTO toSimpleDTO(TenantPO po);

}
