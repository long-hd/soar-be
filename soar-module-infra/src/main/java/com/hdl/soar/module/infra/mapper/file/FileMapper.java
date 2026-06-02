package com.hdl.soar.module.infra.mapper.file;

import com.hdl.soar.module.infra.controller.admin.file.dto.file.FileCreateReqDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.file.FileRespDTO;
import com.hdl.soar.module.infra.dal.entity.file.FilePO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface FileMapper {

    FileMapper INSTANCE = Mappers.getMapper(FileMapper.class);

    FilePO toPO(FileCreateReqDTO dto);

    FileRespDTO toDTO(FilePO po);

    List<FileRespDTO> toDTOList(List<FilePO> list);

}
