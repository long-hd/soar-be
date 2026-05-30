package com.hdl.soar.module.system.controller.admin.dept.dto.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Department List Request DTO")
public class DeptListReqDTO {

    @Schema(description = "Department name (fuzzy match)", example = "Engineering")
    private String name;

    @Schema(description = "Status: 0=Enabled, 1=Disabled", example = "0")
    private Integer status;

}
