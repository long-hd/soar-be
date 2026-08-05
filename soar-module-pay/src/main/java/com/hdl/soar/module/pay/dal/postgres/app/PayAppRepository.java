package com.hdl.soar.module.pay.dal.postgres.app;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.pay.dal.entity.app.PayAppPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayAppRepository extends JpaRepository<PayAppPO, Long>, JpaSpecificationExecutor<PayAppPO> {

    Optional<PayAppPO> findByAppKey(String appKey);

    List<PayAppPO> findAllByStatus(CommonStatusEnum status);

}
