package com.hdl.soar.module.system.mapper;

import com.hdl.soar.module.system.enums.permission.DataScopeEnum;
import com.hdl.soar.module.system.enums.permission.MenuTypeEnum;
import com.hdl.soar.module.system.enums.permission.RoleTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SystemEnumMapper {


    // =============== MenuTypeEnum

    default MenuTypeEnum toMenuTypeEnum(Integer val) {
        return MenuTypeEnum.of(val);
    }

    default Integer toIntMenuTypeEnum(MenuTypeEnum val) {
        return val.getType();
    }

    // =============== RoleTypeEnum

    default RoleTypeEnum toRoleTypeEnum(Integer val) {
        return RoleTypeEnum.of(val);
    }

    default Integer toIntRoleTypeEnum(RoleTypeEnum val) {
        return val.getType();
    }

    // =============== DataScopeEnum

    default DataScopeEnum toDataScopeEnum(Integer val) {
        return DataScopeEnum.of(val);
    }

    default Integer toIntDataScopeEnum(DataScopeEnum val) {
        return val.getScope();
    }

}
