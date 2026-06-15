package com.hdl.soar.module.system.mapper.permission;

import com.hdl.soar.framework.common.mapper.EnumMapper;
import com.hdl.soar.framework.jpa.mapping.SoarMapperConfig;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuRespDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import com.hdl.soar.module.system.mapper.SystemEnumMapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(
        uses = {EnumMapper.class, SystemEnumMapper.class},
        config = SoarMapperConfig.class
)
public interface MenuMapper {
    MenuMapper INSTANCE = Mappers.getMapper(MenuMapper.class);

    MenuPO toPO(MenuSaveReqDTO dto);
    void updatePO(MenuSaveReqDTO dto, @MappingTarget MenuPO po);

    MenuRespDTO toDTO(MenuPO po);
    List<MenuRespDTO> toDTO(List<MenuPO> poList);

    MenuSimpleRespDTO toSimpleDTO(MenuPO po);
    List<MenuSimpleRespDTO> toSimpleDTO(List<MenuPO> poList);

}
