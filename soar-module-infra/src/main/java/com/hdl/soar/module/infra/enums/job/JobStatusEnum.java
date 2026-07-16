package com.hdl.soar.module.infra.enums.job;

import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.Sets;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.quartz.impl.jdbcjobstore.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Status of a scheduled job.
 */
@Getter
@AllArgsConstructor
public enum JobStatusEnum {

    /** Created but not yet registered with the scheduler. */
    INIT(0, Collections.emptySet()),
    /** Registered and running on its schedule. */
    NORMAL(1, Sets.newHashSet(Constants.STATE_WAITING, Constants.STATE_ACQUIRED, Constants.STATE_BLOCKED)),
    /** Registered but paused. */
    STOP(2, Sets.newHashSet(Constants.STATE_PAUSED, Constants.STATE_PAUSED_BLOCKED));


    private final Integer status;
    private final Set<String> quartzStates;

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(JobStatusEnum::getStatus).toArray(Integer[]::new);

    public static JobStatusEnum of(Integer status) {
        return ArrayUtil.firstMatch(e -> e.getStatus().equals(status), values());
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<JobStatusEnum> {
        public JpaConverter() {
            super(JobStatusEnum.class, JobStatusEnum::getStatus);
        }
    }

}
