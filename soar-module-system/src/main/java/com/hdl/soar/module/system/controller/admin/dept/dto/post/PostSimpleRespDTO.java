package com.hdl.soar.module.system.controller.admin.dept.dto.post;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin backend - Simple Position Response VO")
public class PostSimpleRespDTO {

    @Schema(description = "Position ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Position ID")
    private Long id;

    @Schema(description = "Position name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Junior Developer")
    @ExcelProperty("Position name")
    private String name;

}
