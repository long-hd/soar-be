package com.hdl.soar.module.system.controller.admin.dict.dto.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin backend - Simplified Dictionary Type Response DTO")
public class DictTypeSimpleRespDTO {

    @Schema(description = "Dictionary type ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Dictionary type name", requiredMode = Schema.RequiredMode.REQUIRED, example = "example")
    private String name;

    @Schema(description = "Dictionary type code", requiredMode = Schema.RequiredMode.REQUIRED, example = "sys_common_status")
    private String type;

}