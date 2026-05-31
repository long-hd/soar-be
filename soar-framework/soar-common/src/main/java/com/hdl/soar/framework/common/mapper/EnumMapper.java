package com.hdl.soar.framework.common.mapper;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.enums.OperateTypeEnum;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import org.mapstruct.Mapper;

/**
 * Enum <-> Integer conversion methods (MapStruct auto-discovers)
 *
 * <p>Example:</p>
 * <pre>{@code
 * @Mapper(uses = {EnumMapper.class, SystemEnumMapper.class})
 * public interface DictTypeMapper { ... }
 * }</pre>
 */
@Mapper
public interface EnumMapper {

    // =============== CommonStatusEnum

    default CommonStatusEnum toStatusEnum(Integer val) {
        return CommonStatusEnum.of(val);
    }

    default Integer toStatusInt(CommonStatusEnum e) {
        return e == null ? null : e.getStatus();
    }

    // =============== UserTypeEnum

    default UserTypeEnum toUserTypeEnum(Integer val) {
        return UserTypeEnum.of(val);
    }

    default Integer toIntUserTypeEnum(UserTypeEnum e) {
        return e == null ? null : e.getValue();
    }

    // =============== OperateTypeEnum

    default OperateTypeEnum toOperateTypeEnum(Integer val) {
        return OperateTypeEnum.of(val);
    }

    default Integer toIntOperateTypeEnum(OperateTypeEnum e) {
        return e == null ? null : e.getType();
    }

}
