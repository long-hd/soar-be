package com.hdl.soar.module.pay.api.order;

import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hdl.soar.module.pay.api.order.dto.PayOrderRespDTO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
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

    @Override
    public PayOrderRespDTO getOrder(Long id) {
        PayOrderPO order = orderService.getOrder(id);
        PayOrderRespDTO dto = new PayOrderRespDTO();
        dto.setId(order.getId());
        dto.setAppId(order.getAppId());
        dto.setMerchantOrderId(order.getMerchantOrderId());
        dto.setPrice(order.getPrice());
        dto.setCurrency(order.getCurrency() != null ? order.getCurrency().getCode() : null);
        dto.setStatus(order.getStatus() != null ? order.getStatus().getStatus() : null);
        dto.setSuccessTime(order.getSuccessTime());
        dto.setNo(order.getNo());
        dto.setChannelOrderNo(order.getChannelOrderNo());
        return dto;
    }

}
