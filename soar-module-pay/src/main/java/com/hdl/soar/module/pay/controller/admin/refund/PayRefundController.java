package com.hdl.soar.module.pay.controller.admin.refund;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.controller.admin.refund.dto.PayRefundPageReqDTO;
import com.hdl.soar.module.pay.controller.admin.refund.dto.PayRefundRespDTO;
import com.hdl.soar.module.pay.dal.entity.refund.PayRefundPO;
import com.hdl.soar.module.pay.mapper.refund.PayRefundMapper;
import com.hdl.soar.module.pay.service.refund.PayRefundService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Admin Backend - Payment Refund")
@RestController
@RequestMapping("/pay/refund")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayRefundController {

    PayRefundService refundService;

    @GetMapping("/get")
    @Operation(summary = "Get refund")
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PayRefundRespDTO> getRefund(@RequestParam("id") Long id) {
        PayRefundPO refund = refundService.getRefund(id);
        return success(PayRefundMapper.INSTANCE.toDTO(refund));
    }

    @GetMapping("/page")
    @Operation(summary = "Get refund paginated list")
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PageResult<PayRefundRespDTO>> getRefundPage(@Valid PayRefundPageReqDTO pageReqDTO) {
        PageResult<PayRefundPO> page = refundService.getRefundPage(pageReqDTO);
        return success(new PageResult<>(
                PayRefundMapper.INSTANCE.toDTOList(page.getList()), page.getTotal()));
    }

}
