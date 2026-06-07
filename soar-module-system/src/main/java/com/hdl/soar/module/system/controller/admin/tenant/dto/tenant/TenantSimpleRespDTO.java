package com.hdl.soar.module.system.controller.admin.tenant.dto.tenant;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Admin Portal - Tenant Response DTO")
@Data
public class TenantSimpleRespDTO {

    @Schema(description = "Tenant ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Tenant ID")
    private Long id;

    @Schema(description = "Tenant name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Soar")
    @ExcelProperty("Tenant Name")
    private String name;

}
