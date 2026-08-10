package com.hdl.soar.module.pay.job.order;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.quartz.core.handler.JobHandler;
import com.hdl.soar.module.pay.service.order.PayOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * Expire job. Global, same reasoning as {@link PayOrderSyncJob}. Runs less often, e.g. every 5 min.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOrderExpireJob implements JobHandler {

    PayOrderService payOrderService;

    @Override
    public String execute(String param) throws Exception {
        int closed = payOrderService.expireOrder();
        return StrUtil.format("closed={}", closed);
    }

}
