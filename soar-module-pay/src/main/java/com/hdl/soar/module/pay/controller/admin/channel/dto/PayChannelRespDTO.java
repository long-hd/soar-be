package com.hdl.soar.module.pay.controller.admin.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin backend - Payment channel Response DTO")
public class PayChannelRespDTO {

    @Schema(description = "Channel ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Owning app ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long appId;

    @Schema(description = "Channel code", requiredMode = Schema.RequiredMode.REQUIRED, example = "vnpay")
    private String code;

    @Schema(description = "Status, see CommonStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "Fee rate as a percentage", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.5")
    private Double feeRate;

    @Schema(description = "Remark", example = "Some notes")
    private String remark;

    @Schema(description = "Channel configuration as JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    private String config;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createTime;

}
