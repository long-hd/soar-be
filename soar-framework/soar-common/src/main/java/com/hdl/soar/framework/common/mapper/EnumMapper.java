package com.hdl.soar.framework.common.mapper;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
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

}
