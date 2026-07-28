package com.hdl.soar.module.infra.job.job;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.quartz.core.handler.JobHandler;
import com.hdl.soar.module.infra.service.job.JobLogService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * Deletes job logs older than N days. Global (no tenant): {@code infra_job_log} is a global table.
 *
 * <p>Bean name {@code jobLogCleanJob} is the handler name stored in {@code infra_job.handler_name}.
 *
 * <p>Param: retention in days (blank -> 30). Idempotent: re-running only deletes what's still expired.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobLogCleanJob implements JobHandler {

    /** Default retention in days when no param is given. */
    private static final int DEFAULT_EXCEED_DAY = 30;
    /** Rows deleted per batch (see JobLogService#cleanJobLog). */
    private static final int DELETE_LIMIT = 100;

    JobLogService jobLogService;

    @Override
    public String execute(String param) {
        int exceedDay = StrUtil.isBlank(param) ? DEFAULT_EXCEED_DAY : Integer.parseInt(param);
        int count = jobLogService.cleanJobLog(exceedDay, DELETE_LIMIT);
        return StrUtil.format("Cleaned {} job logs older than {} days", count, exceedDay);
    }

}
