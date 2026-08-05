package com.hdl.soar.module.pay.controller.app.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "App - Submit payment order Request DTO")
public class PayOrderSubmitReqDTO {

    @Schema(description = "Order ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Order id cannot be empty")
    private Long id;

    @Schema(description = "Channel code", requiredMode = Schema.RequiredMode.REQUIRED, example = "mock")
    @NotEmpty(message = "Channel code cannot be empty")
    private String channelCode;

    @Schema(description = "Return URL after payment")
    private String returnUrl;

    @Schema(description = "Extra channel parameters")
    private Map<String, String> channelExtras;

}
