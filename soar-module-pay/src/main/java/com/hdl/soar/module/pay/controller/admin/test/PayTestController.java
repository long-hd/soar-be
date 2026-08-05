package com.hdl.soar.module.pay.controller.admin.test;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.pay.api.order.PayOrderApi;
import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hdl.soar.module.pay.api.order.dto.PayOrderRespDTO;
import jakarta.annotation.security.PermitAll;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

/** TEST ONLY — remove before production. Exposes the in-process PayOrderApi over HTTP. */
@RestController
@RequestMapping("/pay/test")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayTestController {

    PayOrderApi orderApi;

    @PostMapping("/create-order")
    @PermitAll
    public CommonResult<Long> createOrder(@RequestBody PayOrderCreateReqDTO reqDTO) {
        return success(orderApi.createOrder(reqDTO));
    }

    @GetMapping("/get-order")
    @PermitAll
    public CommonResult<PayOrderRespDTO> getOrder(@RequestParam("id") Long id) {
        return success(orderApi.getOrder(id));
    }
}