package com.hdl.soar.module.pay.api.refund;

import com.hdl.soar.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import com.hdl.soar.module.pay.service.refund.PayRefundService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayRefundApiImpl implements PayRefundApi {

    PayRefundService refundService;

    @Override
    public Long createRefund(PayRefundCreateReqDTO reqDTO) {
        return refundService.createRefund(reqDTO);
    }

}
