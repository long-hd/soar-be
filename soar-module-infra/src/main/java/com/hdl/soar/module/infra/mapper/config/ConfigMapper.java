package com.hdl.soar.module.infra.mapper.config;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.infra.controller.admin.config.dto.ConfigRespDTO;
import com.hdl.soar.module.infra.controller.admin.config.dto.ConfigSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.config.ConfigPO;
import com.hdl.soar.module.infra.mapper.InfraEnumMapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class, InfraEnumMapper.class})
public interface ConfigMapper {
    ConfigMapper INSTANCE = Mappers.getMapper(ConfigMapper.class);

    @Mapping(source = "key", target = "configKey")
    ConfigPO toPO(ConfigSaveReqDTO dto);

    @Mapping(source = "key", target = "configKey")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePO(ConfigSaveReqDTO dto, @MappingTarget ConfigPO po);

    @Mapping(source = "configKey", target = "key")
    ConfigRespDTO toDTO(ConfigPO po);

    List<ConfigRespDTO> toDTOList(List<ConfigPO> list);

}
