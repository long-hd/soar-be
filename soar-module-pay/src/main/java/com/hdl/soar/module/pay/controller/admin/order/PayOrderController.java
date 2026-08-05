package com.hdl.soar.module.pay.controller.admin.order;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.controller.admin.order.dto.PayOrderPageReqDTO;
import com.hdl.soar.module.pay.controller.admin.order.dto.PayOrderRespDTO;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.mapper.order.PayOrderMapper;
import com.hdl.soar.module.pay.service.order.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Payment Order")
@RestController
@RequestMapping("/pay/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOrderController {

    PayOrderService orderService;

    @GetMapping("/get")
    @Operation(summary = "Get payment order")
    @Parameter(name = "id", description = "Order ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PayOrderRespDTO> getOrder(@RequestParam("id") Long id) {
        PayOrderPO order = orderService.getOrder(id);
        return success(PayOrderMapper.INSTANCE.toDTO(order));
    }

    @GetMapping("/page")
    @Operation(summary = "Get payment order paginated list")
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PageResult<PayOrderRespDTO>> getOrderPage(@Valid PayOrderPageReqDTO pageReqDTO) {
        PageResult<PayOrderPO> pageResult = orderService.getOrderPage(pageReqDTO);
        return success(new PageResult<>(
                PayOrderMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()));
    }

}
