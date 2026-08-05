package com.hdl.soar.module.pay.controller.admin.channel.dto;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Admin backend - Payment channel create/update Request DTO")
public class PayChannelSaveReqDTO {

    @Schema(description = "Channel ID (null on create)", example = "1024")
    private Long id;

    @Schema(description = "Owning app ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "App ID cannot be empty")
    private Long appId;

    @Schema(description = "Channel code, see PayChannelEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "vnpay")
    @NotBlank(message = "Channel code cannot be empty")
    private String code;

    @Schema(description = "Status, see CommonStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "Fee rate as a percentage", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.5")
    @NotNull(message = "Fee rate cannot be empty")
    private Double feeRate;

    @Schema(description = "Remark", example = "Some notes")
    private String remark;

    @Schema(description = "Channel configuration as JSON", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    @NotBlank(message = "Channel config cannot be empty")
    private String config;

}
