package com.hdl.soar.module.pay.controller.app.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "App - Submit payment order Response DTO")
public class PayOrderSubmitRespDTO {

    @Schema(description = "Order status, see PayOrderStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "Display mode, e.g. url / qr_code (null when already paid)", example = "url")
    private String displayMode;

    @Schema(description = "Display content, e.g. the redirect URL", example = "https://sandbox.vnpayment.vn/...")
    private String displayContent;


}
