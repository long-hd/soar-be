package com.hdl.soar.module.system.controller.admin.dict.dto.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin backend - Simplified Data Dictionary Response VO")
public class DictDataSimpleRespDTO {

    @Schema(description = "Dictionary type", requiredMode = Schema.RequiredMode.REQUIRED, example = "gender")
    private String dictType;

    @Schema(description = "Dictionary value", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String value;

    @Schema(description = "Dictionary label", requiredMode = Schema.RequiredMode.REQUIRED, example = "Male")
    private String label;

    @Schema(description = "Color type: default, primary, success, info, warning, danger", example = "default")
    private String colorType;

    @Schema(description = "CSS class", example = "btn-visible")
    private String cssClass;

}
