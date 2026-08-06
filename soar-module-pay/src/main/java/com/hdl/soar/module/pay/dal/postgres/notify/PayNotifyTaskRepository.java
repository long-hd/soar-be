package com.hdl.soar.module.pay.dal.postgres.notify;

import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyTaskPO;
import com.hdl.soar.module.pay.enums.notify.PayNotifyStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PayNotifyTaskRepository extends JpaRepository<PayNotifyTaskPO, Long>,
        JpaSpecificationExecutor<PayNotifyTaskPO> {

    /**
     * Due tasks for the poll relay: WAITING and past their next-notify time, oldest first, capped.
     * Tenant filter is applied automatically (the poll job runs per tenant via {@code @TenantJob}).
     */
    List<PayNotifyTaskPO> findTop200ByStatusAndNextNotifyTimeLessThanEqualOrderByNextNotifyTimeAsc(
            PayNotifyStatusEnum status, Instant now);

}