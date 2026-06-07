package com.hdl.soar.module.system.controller.admin.tenant.dto.tenant;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.hdl.soar.framework.excel.core.annotations.DictFormat;
import com.hdl.soar.framework.excel.core.convert.DictConvert;
import com.hdl.soar.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Schema(description = "Admin Portal - Tenant Response DTO")
@Data
@ExcelIgnoreUnannotated
public class TenantRespDTO {

    @Schema(description = "Tenant ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Tenant ID")
    private Long id;

    @Schema(description = "Tenant name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Soar")
    @ExcelProperty("Tenant Name")
    private String name;

    @Schema(description = "Contact person", requiredMode = Schema.RequiredMode.REQUIRED, example = "HDL")
    @ExcelProperty("Contact Person")
    private String contactName;

    @Schema(description = "Contact mobile number", example = "15601691300")
    @ExcelProperty("Contact Mobile")
    private String contactMobile;

    @Schema(description = "Tenant status", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "Status", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "Bound website domains", example = "https://www.soar.com")
    private List<String> websites;

    @Schema(description = "Tenant package ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long packageId;

    @Schema(description = "Expiration time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant expireTime;

    @Schema(description = "Maximum account count", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer accountCount;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Creation Time")
    private Instant createTime;

}
