package com.hdl.soar.module.pay.api.order;

import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hdl.soar.module.pay.service.order.PayOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOrderApiImpl implements PayOrderApi {

    PayOrderService orderService;

    @Override
    public Long createOrder(PayOrderCreateReqDTO reqDTO) {
        return orderService.createOrder(reqDTO);
    }

}
