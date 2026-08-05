package com.hdl.soar.module.pay.controller.admin.app.dto;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin backend - Payment app create/update Request DTO")
public class PayAppSaveReqDTO {

    @Schema(description = "App ID (null on create)", example = "1024")
    private Long id;

    @Schema(description = "App key", requiredMode = Schema.RequiredMode.REQUIRED, example = "demo-app")
    @NotBlank(message = "App key cannot be empty")
    @Size(max = 64, message = "App key length cannot exceed 64 characters")
    private String appKey;

    @Schema(description = "App name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Demo store")
    @NotBlank(message = "App name cannot be empty")
    @Size(max = 64, message = "App name length cannot exceed 64 characters")
    private String name;

    @Schema(description = "Status, see CommonStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "Remark", example = "Some notes")
    private String remark;

    @Schema(description = "Order success callback URL", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://example.com/pay/order/notify")
    @NotBlank(message = "Order notify URL cannot be empty")
    @Size(max = 1024, message = "Order notify URL length cannot exceed 1024 characters")
    private String orderNotifyUrl;

    @Schema(description = "Refund callback URL", example = "https://example.com/pay/refund/notify")
    @Size(max = 1024, message = "Refund notify URL length cannot exceed 1024 characters")
    private String refundNotifyUrl;

    @Schema(description = "Transfer callback URL", example = "https://example.com/pay/transfer/notify")
    @Size(max = 1024, message = "Transfer notify URL length cannot exceed 1024 characters")
    private String transferNotifyUrl;

}