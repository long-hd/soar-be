package com.hdl.soar.module.pay.controller.admin.test;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.tenant.core.aop.TenantIgnore;
import com.hdl.soar.module.pay.api.order.PayOrderApi;
import com.hdl.soar.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hdl.soar.module.pay.api.order.dto.PayOrderRespDTO;
import com.hdl.soar.module.pay.service.order.PayOrderService;
import jakarta.annotation.security.PermitAll;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

/** TEST ONLY — remove before production. Exposes the in-process PayOrderApi over HTTP. */
@Slf4j
@RestController
@RequestMapping("/pay/test")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayTestController {

    // ===== test: merchant notify sink =====
    private static volatile boolean SINK_OK = true;

    PayOrderApi orderApi;
    PayOrderService payOrderService;

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

    @PostMapping("/notify-sink")
    @PermitAll
    @TenantIgnore // notify gửi kèm header tenant-id nhưng sink không cần resolve tenant
    public CommonResult<Boolean> notifySink(@RequestBody Map<String, Object> body) {
        log.info("[notify-sink] nhận: {}", body);
        if (!SINK_OK) {
            return CommonResult.error(500, "forced fail for test");
        }
        return CommonResult.success(true);
    }

    @GetMapping("/notify-sink/toggle")
    @PermitAll
    @TenantIgnore
    public CommonResult<Boolean> toggleSink(@RequestParam("ok") boolean ok) {
        SINK_OK = ok;
        return CommonResult.success(ok);
    }

    @GetMapping("/sync-order")
    @PermitAll
    public CommonResult<Integer> syncOrder() {
        return success(payOrderService.syncOrder());
    }

}