package com.hdl.soar.module.system.mapper;

import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.module.system.enums.common.SexEnum;
import com.hdl.soar.module.system.enums.logger.LoginLogTypeEnum;
import com.hdl.soar.module.system.enums.logger.LoginResultEnum;
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

    // =============== SexEnum

    default SexEnum toSexEnum(Integer val) { return SexEnum.of(val); }

    default Integer toIntSexEnum(SexEnum val) { return  val.getSex(); }

    // =============== LoginLogTypeEnum

    default LoginLogTypeEnum toLoginLogTypeEnum(Integer val) {
        return LoginLogTypeEnum.of(val);
    }

    default Integer toIntLoginLogTypeEnum(LoginLogTypeEnum val) {
        return val.getType();
    }

    // =============== UserTypeEnum

    default UserTypeEnum toUserTypeEnum(Integer val) {
        return UserTypeEnum.of(val);
    }

    default Integer toIntUserTypeEnum(UserTypeEnum val) {
        return val.getValue();
    }

    // =============== LoginResultEnum

    default LoginResultEnum toLoginResultEnum(Integer val) {
        return LoginResultEnum.of(val);
    }

    default  Integer toIntLoginResultEnum(LoginResultEnum val) {
        return val.getResult();
    }

}
