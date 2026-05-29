package com.hdl.soar.module.system.mapper;

import com.hdl.soar.module.system.enums.permission.MenuTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SystemEnumMapper {

    default MenuTypeEnum toMenuTypeEnum(Integer val) {
        return MenuTypeEnum.of(val);
    }

    default Integer toIntMenuTypeEnum(MenuTypeEnum val) {
        return val.getType();
    }

}
