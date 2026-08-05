package com.hdl.soar.module.pay.controller.admin.order.dto;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin backend - Payment order pagination Request DTO")
public class PayOrderPageReqDTO extends PageParam {

    @Schema(description = "App ID", example = "1024")
    private Long appId;

    @Schema(description = "Channel code", example = "vnpay")
    private String channelCode;

    @Schema(description = "Merchant order id, fuzzy match", example = "ORD-1")
    private String merchantOrderId;

    @Schema(description = "Payment no, fuzzy match", example = "P202608...")
    private String no;

    @Schema(description = "Status, see PayOrderStatusEnum", example = "10")
    private Integer status;

}
