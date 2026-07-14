package com.hdl.soar.framework.tenant.core.context;

import com.alibaba.ttl.TtlRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves that {@link TenantContextHolder} (a TransmittableThreadLocal) is
 * propagated
 * from the submitting thread to a pooled worker thread when the executor is
 * decorated
 * with {@code TtlRunnable::get} — mirroring the production wiring in
 * SoarAsyncAutoConfiguration.
 *
 * <p>
 * Key technique: corePoolSize == maxPoolSize == 1. Forcing every task onto ONE
 * physical,
 * reused thread makes a stale-context leak deterministic. With a larger pool
 * the leak is
 * non-deterministic (a fresh thread might hide it), so single-thread is what
 * turns a flaky
 * observation into a reliable regression test.
 */
public class AsyncTenantContextPropagationTest {

    private ThreadPoolTaskExecutor executor;

    @BeforeEach
    void setup() {
        // Mirror the production asyncExecutor, but pinned to a single reused thread.
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("test-async-");
        // The line under test. Comment it out to watch the leak test below fail
        // ("prove-the-bug-first"): without it, the reused worker keeps a stale tenant.
        executor.setTaskDecorator(TtlRunnable::get);
        executor.initialize();
    }

    @AfterEach
    void tearDown() {
        executor.shutdown();
        TenantContextHolder.clear(); // never leak context into other tests
    }

    @Test
    @DisplayName("async task sees the tenant of the submitting thread")
    void asyncTask_seesSubmitterTenant() throws Exception {
        TenantContextHolder.setTenantId(1L);

        assertThat(runOnPoolAndReadTenant()).isEqualTo(1L);
    }

    @Test
    @DisplayName("reused worker thread does not leak a stale tenant across tasks")
    void reusedThread_doesNotLeakStaleTenant() throws Exception {
        // Task A runs under tenant 1 on the single pooled thread.
        TenantContextHolder.setTenantId(1L);
        assertThat(runOnPoolAndReadTenant()).isEqualTo(1L);

        // Task B runs under tenant 2 on the SAME physical thread.
        // Without the decorator's capture/replay/restore, the worker would still
        // hold tenant 1 (leftover) -> this is the exact production bug being guarded.
        TenantContextHolder.setTenantId(2L);
        assertThat(runOnPoolAndReadTenant()).isEqualTo(2L);

        // A submit with no tenant set must observe null - not a leftover value.
        TenantContextHolder.clear();
        assertThat(runOnPoolAndReadTenant()).isNull();
    }

    /**
     * Runs a task on the executor that reads the tenant ON THE WORKER THREAD and returns it.
     * Uses execute(Runnable) (not submit) because the TaskDecorator is applied on the execute path.
     */
    private Long runOnPoolAndReadTenant() throws InterruptedException {
        Thread callerThread = Thread.currentThread();
        AtomicReference<Long> tenantSeenOnWorker = new AtomicReference<>();
        AtomicReference<Thread> workerThread = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        executor.execute(() -> {
            workerThread.set(Thread.currentThread());
            tenantSeenOnWorker.set(TenantContextHolder.getTenantId());
            done.countDown();
        });

        assertThat(done.await(2, TimeUnit.SECONDS)).as("task should finish").isTrue();
        // Sanity: we actually crossed a thread boundary (otherwise the test proves nothing).
        assertThat(workerThread.get()).isNotSameAs(callerThread);
        return tenantSeenOnWorker.get();
    }

}
