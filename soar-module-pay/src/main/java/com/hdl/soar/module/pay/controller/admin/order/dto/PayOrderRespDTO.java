package com.hdl.soar.module.pay.controller.admin.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Schema(description = "Admin backend - Payment order Response DTO")
public class PayOrderRespDTO {

    @Schema(description = "Order ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "App ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long appId;

    @Schema(description = "Winning channel ID")
    private Long channelId;

    @Schema(description = "Winning channel code")
    private String channelCode;

    @Schema(description = "Merchant order id", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD-1")
    private String merchantOrderId;

    @Schema(description = "Product title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subject;

    @Schema(description = "Product description")
    private String body;

    @Schema(description = "Amount", requiredMode = Schema.RequiredMode.REQUIRED, example = "100000")
    private BigDecimal price;

    @Schema(description = "Currency (ISO 4217)", requiredMode = Schema.RequiredMode.REQUIRED, example = "VND")
    private String currency;

    @Schema(description = "Channel fee rate (percent)")
    private Double channelFeeRate;

    @Schema(description = "Channel fee amount")
    private BigDecimal channelFeePrice;

    @Schema(description = "Status, see PayOrderStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "Expiry time")
    private Instant expireTime;

    @Schema(description = "Success time")
    private Instant successTime;

    @Schema(description = "Winning extension id")
    private Long extensionId;

    @Schema(description = "Winning payment no")
    private String no;

    @Schema(description = "Refunded amount")
    private BigDecimal refundPrice;

    @Schema(description = "Channel order number")
    private String channelOrderNo;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createTime;

}
