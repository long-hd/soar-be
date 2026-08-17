package com.hdl.soar.module.pay.controller.admin.refund.dto;

import com.hdl.soar.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PayRefundPageReqDTO extends PageParam {
    private Long appId;
    private String channelCode;
    private String merchantOrderId;
    private String merchantRefundId;
    private Integer status;
}
