package com.hdl.soar.framework.quartz.config;

import com.hdl.soar.framework.quartz.core.scheduler.SchedulerManager;
import com.hdl.soar.framework.quartz.core.service.JobLogFrameworkService;
import com.hdl.soar.framework.quartz.core.service.NoOpJobLogFrameworkService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for scheduled jobs.
 *
 * <p>Loads after Spring Boot's Quartz auto-configuration, since {@link SchedulerManager}
 * wraps the {@link Scheduler} that it creates.
 */
@AutoConfiguration(after = QuartzAutoConfiguration.class)
public class SoarQuartzAutoConfiguration {

    /**
     * ObjectProvider keeps this resolvable even when Quartz is absent — the manager then
     * fails with a clear "jobs are disabled" error instead of the context failing to start.
     */
    @Bean
    public SchedulerManager schedulerManager(ObjectProvider<Scheduler> scheduler) {
        return new SchedulerManager(scheduler.getIfAvailable());
    }

    /**
     * Only used until a module provides a persisting implementation.
     */
    @Bean
    @ConditionalOnMissingBean(JobLogFrameworkService.class)
    public JobLogFrameworkService noOpJobLogFrameworkService() {
        return new NoOpJobLogFrameworkService();
    }

}
