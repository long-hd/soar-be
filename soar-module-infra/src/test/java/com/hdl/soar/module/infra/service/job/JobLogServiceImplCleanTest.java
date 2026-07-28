package com.hdl.soar.module.infra.service.job;

import com.hdl.soar.module.infra.dal.postgres.job.JobLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Verifies the batch-delete loop in cleanJobLog - the tricky part is the stop condition.
 * The repository is mocked; we script its per-call return values to drive the loop.
 */
public class JobLogServiceImplCleanTest {

    private static final int LIMIT = 100;

    private final JobLogRepository jobLogRepository = mock(JobLogRepository.class);
    private final JobLogServiceImpl service = new JobLogServiceImpl(jobLogRepository);

    @Test
    @DisplayName("loops until a batch returns fewer than the limit, summing all deletions")
    void cleanJobLog_loopsAndSums() {
        // Two full batches (100, 100), then a partial one (30) -> loop stops after the 30.
        when(jobLogRepository.deleteByCreateTimeLtWithLimit(any(Instant.class), eq(LIMIT)))
                .thenReturn(LIMIT, LIMIT, 30);

        int total = service.cleanJobLog(7, LIMIT);

        assertThat(total).isEqualTo(230);
        // exactly 3 calls: two full + one partial that ends the loop
        verify(jobLogRepository, times(3)).deleteByCreateTimeLtWithLimit(any(Instant.class), eq(LIMIT));
    }

    @Test
    @DisplayName("stops after one call when the first batch is already partial")
    void cleanJobLog_singlePartialBatch() {
        when(jobLogRepository.deleteByCreateTimeLtWithLimit(any(Instant.class), eq(LIMIT)))
                .thenReturn(10); // fewer than limit on the very first call

        int total = service.cleanJobLog(7, LIMIT);

        assertThat(total).isEqualTo(10);
        verify(jobLogRepository, times(1)).deleteByCreateTimeLtWithLimit(any(Instant.class), eq(LIMIT));
    }

    @Test
    @DisplayName("nothing to delete -> zero, one call")
    void cleanJobLog_nothing() {
        when(jobLogRepository.deleteByCreateTimeLtWithLimit(any(Instant.class), eq(LIMIT)))
                .thenReturn(0);

        int total = service.cleanJobLog(7, LIMIT);

        assertThat(total).isZero();
        verify(jobLogRepository, times(1)).deleteByCreateTimeLtWithLimit(any(Instant.class), eq(LIMIT));
    }

}
