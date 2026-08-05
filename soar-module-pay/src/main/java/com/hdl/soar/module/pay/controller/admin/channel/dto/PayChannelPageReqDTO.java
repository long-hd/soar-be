package com.hdl.soar.module.pay.controller.admin.channel.dto;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin backend - Payment channel pagination Request DTO")
public class PayChannelPageReqDTO extends PageParam {

    @Schema(description = "Owning app ID", example = "1024")
    private Long appId;

    @Schema(description = "Channel code", example = "vnpay")
    private String code;

    @Schema(description = "Status, see CommonStatusEnum", example = "0")
    private Integer status;

}
