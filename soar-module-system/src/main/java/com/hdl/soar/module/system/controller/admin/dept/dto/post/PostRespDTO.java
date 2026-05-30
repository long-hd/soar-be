package com.hdl.soar.module.system.controller.admin.dept.dto.post;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.hdl.soar.framework.excel.core.annotations.DictFormat;
import com.hdl.soar.framework.excel.core.convert.DictConvert;
import com.hdl.soar.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@ExcelIgnoreUnannotated
@Schema(description = "Admin backend - Position information Response DTO")
public class PostRespDTO {

    @Schema(description = "Position ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Position ID")
    private Long id;

    @Schema(description = "Position name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Junior Developer")
    @ExcelProperty("Position name")
    private String name;

    @Schema(description = "Position code", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    @ExcelProperty("Position code")
    private String code;

    @Schema(description = "Display order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Position sort")
    private Integer sort;

    @Schema(description = "Status, see CommonStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "Status", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "Remark", example = "Some notes")
    private String remark;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createTime;
}
