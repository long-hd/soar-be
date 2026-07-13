package com.hdl.soar.framework.web.config;

import com.alibaba.ttl.TtlRunnable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async configuration for the Soar framework.
 * <p>
 * Enables Spring's {@code @Async} support globally and provides
 * a default thread pool for async operations (e.g., API access log persistence).
 * <p>
 * The task decorator wraps every submitted task with {@link TtlRunnable} so that
 * {@code TransmittableThreadLocal} state (tenant context + security context) is
 * captured on the submitting thread and replayed on the pooled worker thread,
 * then cleaned up. Without it, pooled threads retain stale tenant context across
 * tasks — silently mislabeling async-written logs under concurrent multi-tenant load.
 */
@Slf4j
@AutoConfiguration
@EnableAsync
public class SoarAsyncAutoConfiguration {

    @Bean("asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("soar-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setTaskDecorator(TtlRunnable::get);
        executor.initialize();
        log.info("[asyncExecutor][Initialized thread pool: core={}, max={}, queue={}]",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        return executor;
    }

}
