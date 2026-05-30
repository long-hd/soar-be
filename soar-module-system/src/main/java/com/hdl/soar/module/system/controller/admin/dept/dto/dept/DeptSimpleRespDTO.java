package com.hdl.soar.module.system.controller.admin.dept.dto.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Department Simple Response DTO")
public class DeptSimpleRespDTO {

    @Schema(description = "Department ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Department name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Engineering")
    private String name;

    @Schema(description = "Parent department ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long parentId;

}
