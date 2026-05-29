package com.hdl.soar.module.system.controller.admin.permission.dto.role;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.hdl.soar.framework.excel.core.annotations.DictFormat;
import com.hdl.soar.framework.excel.core.convert.DictConvert;
import com.hdl.soar.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@ExcelIgnoreUnannotated
@Schema(description = "Admin backend - Role response DTO")
public class RoleRespDTO {

    @Schema(description = "Role ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("Role ID")
    private Long id;

    @Schema(description = "Role name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Administrator")
    @ExcelProperty("Role name")
    private String name;

    @Schema(description = "Role code", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "Role code cannot be empty")
    @ExcelProperty("Role code")
    private String code;

    @Schema(description = "Display order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Role sort")
    private Integer sort;

    @Schema(description = "Status, see CommonStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "Role status", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "Role type, see RoleTypeEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "Remark", example = "I am a role")
    private String remark;

    @Schema(description = "Data scope, see DataScopeEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "Data scope", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.DATA_SCOPE)
    private Integer dataScope;

    @Schema(description = "Data scope (dept ID list)", example = "1")
    private Set<Long> dataScopeDeptIds;

    @Schema(description = "Create time", requiredMode = Schema.RequiredMode.REQUIRED, example = "timestamp")
    private Instant createTime;

}
