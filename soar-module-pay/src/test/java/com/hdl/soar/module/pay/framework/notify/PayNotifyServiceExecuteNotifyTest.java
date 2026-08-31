package com.hdl.soar.module.pay.framework.notify;

import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyTaskPO;
import com.hdl.soar.module.pay.dal.postgres.notify.PayNotifyLogRepository;
import com.hdl.soar.module.pay.dal.postgres.notify.PayNotifyTaskRepository;
import com.hdl.soar.module.pay.dal.redis.notify.PayNotifyLockRedisDAO;
import com.hdl.soar.module.pay.enums.notify.PayNotifyStatusEnum;
import com.hdl.soar.module.pay.framework.notify.core.producer.PayNotifyProducer;
import com.hdl.soar.module.pay.service.notify.PayNotifyServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** executeNotify() re-publishes every due task and returns the count actually handed to the broker. */
public class PayNotifyServiceExecuteNotifyTest {

    private final PayNotifyTaskRepository taskRepository = mock(PayNotifyTaskRepository.class);
    private final PayNotifyLogRepository logRepository = mock(PayNotifyLogRepository.class);
    private final PayNotifyLockRedisDAO lockRedisDAO = mock(PayNotifyLockRedisDAO.class);
    private final RestClient restClient = mock(RestClient.class);
    private final PayNotifyProducer notifyProducer = mock(PayNotifyProducer.class);

    private final PayNotifyServiceImpl service = new PayNotifyServiceImpl(
            taskRepository, logRepository, lockRedisDAO, restClient, notifyProducer);

    @AfterEach
    void clearTenant() {
        TenantContextHolder.setTenantId(null);
    }

    @Test
    void executeNotify_republishesEveryDueTask_andCountsSuccesses() {
        TenantContextHolder.setTenantId(1L); // @TenantJob sets this in production
        PayNotifyTaskPO t1 = mock(PayNotifyTaskPO.class);
        PayNotifyTaskPO t2 = mock(PayNotifyTaskPO.class);
        PayNotifyTaskPO t3 = mock(PayNotifyTaskPO.class);
        when(t1.getId()).thenReturn(101L);
        when(t2.getId()).thenReturn(102L);
        when(t3.getId()).thenReturn(103L);
        when(taskRepository.findTop200ByStatusAndNextNotifyTimeLessThanEqualOrderByNextNotifyTimeAsc(
                eq(PayNotifyStatusEnum.WAITING), any(Instant.class)))
                .thenReturn(List.of(t1, t2, t3));
        // third publish reports a broker failure -> not counted
        when(notifyProducer.publish(anyLong(), eq(1L))).thenReturn(true, true, false);

        int published = service.executeNotify();

        assertThat(published).isEqualTo(2);
        verify(notifyProducer).publish(101L, 1L);
        verify(notifyProducer).publish(102L, 1L);
        verify(notifyProducer).publish(103L, 1L);
    }

    @Test
    void executeNotify_noDueTasks_returnsZero_andPublishesNothing() {
        TenantContextHolder.setTenantId(1L);
        when(taskRepository.findTop200ByStatusAndNextNotifyTimeLessThanEqualOrderByNextNotifyTimeAsc(
                eq(PayNotifyStatusEnum.WAITING), any(Instant.class)))
                .thenReturn(List.of());

        assertThat(service.executeNotify()).isZero();
    }

}
