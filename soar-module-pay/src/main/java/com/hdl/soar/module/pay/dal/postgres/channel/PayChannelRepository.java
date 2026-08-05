package com.hdl.soar.module.pay.dal.postgres.channel;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PayChannelRepository extends JpaRepository<PayChannelPO, Long>,
        JpaSpecificationExecutor<PayChannelPO> {

    Optional<PayChannelPO> findByAppIdAndCode(Long appId, String code);

    List<PayChannelPO> findAllByAppId(Long appId);

    List<PayChannelPO> findAllByStatus(CommonStatusEnum status);

}
