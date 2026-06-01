package com.hdl.soar.module.infra.mapper;

import com.hdl.soar.module.infra.enums.logger.ApiErrorLogProcessStatusEnum;
import org.mapstruct.Mapper;

/**
 * Enum <-> Integer conversions specific to the Infra module.
 */
@Mapper
public interface InfraEnumMapper {

    // =============== ApiErrorLogProcessStatusEnum

    default ApiErrorLogProcessStatusEnum toProcessStatusEnum(Integer val) {
        return ApiErrorLogProcessStatusEnum.of(val);
    }

    default Integer toIntProcessStatus(ApiErrorLogProcessStatusEnum e) {
        return e == null ? null : e.getStatus();
    }

}
