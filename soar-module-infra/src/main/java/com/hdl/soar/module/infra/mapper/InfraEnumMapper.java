package com.hdl.soar.module.infra.mapper;

import com.hdl.soar.module.infra.enums.job.JobLogStatusEnum;
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

    // =============== JobLogStatusEnum

    default JobLogStatusEnum toJobLogStatusEnum(Integer val) {
        return JobLogStatusEnum.of(val);
    }

    default Integer toIntJobLogStatus(JobLogStatusEnum e) {
        return e == null ? null : e.getStatus();
    }

}
