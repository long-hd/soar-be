package com.hdl.soar.module.pay.mapper;

import com.hdl.soar.module.pay.enums.PayRefundStatusEnum;
import com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum;
import org.mapstruct.Mapper;

@Mapper
public interface PayEnumMapper {

    // =========== PayOrderStatusEnum

    default PayOrderStatusEnum toOrderStatusEnum(Integer val) {
        return PayOrderStatusEnum.of(val);
    }

    default Integer toOrderStatusInt(PayOrderStatusEnum e) {
        return e == null ? null : e.getStatus();
    }

    // =========== PayOrderStatusEnum

    default PayRefundStatusEnum toRefundStatusEnum(Integer val) {
        return PayRefundStatusEnum.of(val);
    }

    default Integer toRefundStatusInt(PayRefundStatusEnum e) {
        return e == null ? null : e.getStatus();
    }

}
