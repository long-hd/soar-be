package com.hdl.soar.framework.tenant.core.job;

import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.tenant.core.service.TenantFrameworkService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the per-tenant iteration contract of {@link TenantJobAspect}.
 *
 * <p>The ProceedingJoinPoint is mocked: on each proceed() it records the tenant id visible
 * at that moment — which is exactly what the aspect is supposed to be setting.
 */
public class TenantJobAspectTest {

    private TenantFrameworkService tenantFrameworkService;
    private TenantJobAspect aspect;

    /** Tenant id observed inside the job body, one entry per execution. */
    private List<Long> observedTenantIds;

    @BeforeEach
    void setUp() {
        tenantFrameworkService = mock(TenantFrameworkService.class);
        aspect = new TenantJobAspect(tenantFrameworkService);
        observedTenantIds = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("job body runs once per tenant, each with that tenant's context")
    void runsOncePerTenant_withCorrectContext() throws Throwable {
        when(tenantFrameworkService.getTenantIds()).thenReturn(List.of(1L, 2L, 3L));

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            observedTenantIds.add(TenantContextHolder.getTenantId()); // context at execution time
            return "ok";
        });

        Object result = aspect.around(joinPoint, mock(TenantJob.class));

        // Ran 3 times, each under the right tenant — the whole point of @TenantJob.
        assertThat(observedTenantIds).containsExactly(1L, 2L, 3L);
        assertThat(result).asString().contains("ok");
    }

    @Test
    @DisplayName("one failing tenant does not block the others")
    void failingTenant_doesNotBlockOthers() throws Throwable {
        when(tenantFrameworkService.getTenantIds()).thenReturn(List.of(1L, 2L, 3L));

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Long tenantId = TenantContextHolder.getTenantId();
            observedTenantIds.add(tenantId);
            if (tenantId == 2L) {
                throw new IllegalStateException("boom");
            }
            return "ok";
        });

        Object result = aspect.around(joinPoint, mock(TenantJob.class));

        // Tenant 3 still ran even though tenant 2 blew up.
        assertThat(observedTenantIds).containsExactly(1L, 2L, 3L);
        assertThat(result).asString().contains("job failed").contains("boom");
    }

    @Test
    @DisplayName("context is restored after the job finishes")
    void restoresContext_afterExecution() throws Throwable {
        when(tenantFrameworkService.getTenantIds()).thenReturn(List.of(1L, 2L));
        TenantContextHolder.setTenantId(99L); // pre-existing context

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("ok");

        aspect.around(joinPoint, mock(TenantJob.class));

        // TenantUtils.execute must put the original context back.
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("no tenants -> job body never runs")
    void noTenants_bodyNotExecuted() throws Throwable {
        when(tenantFrameworkService.getTenantIds()).thenReturn(List.of());

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            observedTenantIds.add(TenantContextHolder.getTenantId());
            return "ok";
        });

        Object result = aspect.around(joinPoint, mock(TenantJob.class));

        assertThat(observedTenantIds).isEmpty();
        assertThat(result).isNull();
    }

}
