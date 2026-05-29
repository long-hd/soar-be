package com.hdl.soar.module.system.controller.admin.permission.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Menu List Request DTO")
public class MenuListReqDTO {

    @Schema(description = "Menu name, fuzzy match", example = "Soar")
    private String name;

    @Schema(description = "Display status, see CommonStatusEnum enum", example = "1")
    private Integer status;

}
