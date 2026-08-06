package com.hdl.soar.module.pay.controller.admin.notify.dto;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin backend - Notify task pagination Request DTO")
public class PayNotifyTaskPageReqDTO extends PageParam {

    @Schema(description = "App ID", example = "1024")
    private Long appId;

    @Schema(description = "Merchant order id, fuzzy match", example = "ORD-1")
    private String merchantOrderId;

    @Schema(description = "Status, see PayNotifyStatusEnum", example = "0")
    private Integer status;

}
