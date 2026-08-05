package com.hdl.soar.module.pay.controller.admin.app.dto;


import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin backend - Payment app pagination Request DTO")
public class PayAppPageReqDTO extends PageParam {

    @Schema(description = "App name, fuzzy match", example = "store")
    private String name;

    @Schema(description = "Status, see CommonStatusEnum", example = "0")
    private Integer status;

}