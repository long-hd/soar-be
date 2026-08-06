package com.hdl.soar.module.pay.framework.job.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Wires the dedicated thread pool used by {@code PayNotifyJob} for outbound notify HTTP calls.
 * <p>
 * Kept off the shared async executor so a slow merchant cannot starve unrelated {@code @Async} work.
 * {@code CallerRunsPolicy} applies back-pressure: when the queue is full the submitting thread runs
 * the task itself instead of dropping it.
 */
@Configuration(proxyBeanMethods = false)
public class PayJobConfiguration {

    /** Bean name of the dedicated notify HTTP executor. */
    public static final String NOTIFY_EXECUTOR = "payNotifyExecutor";

    @Bean(NOTIFY_EXECUTOR)
    public Executor payNotifyExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("pay-notify-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

}
