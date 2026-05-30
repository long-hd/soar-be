package com.hdl.soar.module.system.mapper.dept;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostRespDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.dept.PostPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {EnumMapper.class})
public interface PostMapper {
    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    PostPO toPO(PostSaveReqDTO dto);
    void updatePO(PostSaveReqDTO dto, @MappingTarget PostPO po);

    PostRespDTO toDTO(PostPO po);
    List<PostRespDTO> toDTOList(List<PostPO> poList);

    PostSimpleRespDTO toSimpleDTO(PostPO po);
    List<PostSimpleRespDTO> toSimpleDTOList(List<PostPO> poList);

}
