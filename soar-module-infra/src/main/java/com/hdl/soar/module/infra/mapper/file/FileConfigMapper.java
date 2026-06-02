package com.hdl.soar.module.infra.mapper.file;

import com.hdl.soar.module.infra.controller.admin.file.dto.config.FileConfigRespDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.config.FileConfigSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.file.FileConfigPO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface FileConfigMapper {

    FileConfigMapper INSTANCE = Mappers.getMapper(FileConfigMapper.class);

    @Mapping(target = "config", ignore = true) // set in service from validated config map
    FileConfigPO toPO(FileConfigSaveReqDTO dto);

    @Mapping(target = "config", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePO(FileConfigSaveReqDTO dto, @MappingTarget FileConfigPO po);

    @Mapping(target = "config", ignore = true) // set in service by parsing the JSON string
    FileConfigRespDTO toDTO(FileConfigPO po);

    List<FileConfigRespDTO> toDTOList(List<FileConfigPO> list);

}
