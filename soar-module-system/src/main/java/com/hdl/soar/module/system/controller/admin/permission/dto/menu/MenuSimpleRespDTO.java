package com.hdl.soar.module.system.controller.admin.permission.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Menu Simple Response DTO")
public class MenuSimpleRespDTO {

    @Schema(description = "Menu ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Menu name", requiredMode = Schema.RequiredMode.REQUIRED, example = "User Management")
    private String name;

    @Schema(description = "Parent menu ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long parentId;

    @Schema(description = "Type: 1=Directory, 2=Menu, 3=Button", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

}
