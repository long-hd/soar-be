package com.hdl.soar.module.pay.controller.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin backend - Payment app Response DTO")
public class PayAppRespDTO {

    @Schema(description = "App ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "App key", requiredMode = Schema.RequiredMode.REQUIRED, example = "demo-app")
    private String appKey;

    @Schema(description = "App name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Demo store")
    private String name;

    @Schema(description = "Status, see CommonStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "Remark", example = "Some notes")
    private String remark;

    @Schema(description = "Order success callback URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNotifyUrl;

    @Schema(description = "Refund callback URL")
    private String refundNotifyUrl;

    @Schema(description = "Transfer callback URL")
    private String transferNotifyUrl;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createTime;

}
