package com.hdl.soar.module.pay.dal.postgres.notify;

import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyLogPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayNotifyLogRepository extends JpaRepository<PayNotifyLogPO, Long> {

    List<PayNotifyLogPO> findAllByTaskIdOrderByNotifyTimesAsc(Long taskId);

}