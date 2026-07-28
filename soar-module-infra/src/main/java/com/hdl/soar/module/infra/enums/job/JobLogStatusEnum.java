package com.hdl.soar.module.infra.enums.job;

import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Status of a single job execution.
 */
@Getter
@AllArgsConstructor
public enum JobLogStatusEnum {

    RUNNING(0),
    SUCCESS(1),
    FAILURE(2);

    private final Integer status;

    public static JobLogStatusEnum of(Integer status) {
        return ArrayUtil.firstMatch(e -> e.getStatus().equals(status), values());
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<JobLogStatusEnum> {
        public JpaConverter() {
            super(JobLogStatusEnum.class, JobLogStatusEnum::getStatus);
        }
    }

}
