package com.hdl.soar.module.pay.controller.app.order;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.module.pay.controller.app.order.dto.PayOrderSubmitReqDTO;
import com.hdl.soar.module.pay.controller.app.order.dto.PayOrderSubmitRespDTO;
import com.hdl.soar.module.pay.service.order.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

/**
 * App-facing payment order endpoints.
 * <p>
 * {@code submit} is {@code @PermitAll} for now: real app-facing auth (merchant API-key + request
 * signature) is a dedicated future. Submit is exercised via a created order id.
 */
@Tag(name = "App - Payment Order")
@RestController
@RequestMapping("/pay/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppPayOrderController {

    PayOrderService orderService;

    @PostMapping("/submit")
    @Operation(summary = "Submit a payment order (choose a channel, get a pay URL)")
    @PermitAll
    public CommonResult<PayOrderSubmitRespDTO> submitOrder(@Valid @RequestBody PayOrderSubmitReqDTO reqDTO,
                                                           HttpServletRequest request) {
        return success(orderService.submitOrder(reqDTO, ServletUtils.getClientIP(request)));
    }

}
