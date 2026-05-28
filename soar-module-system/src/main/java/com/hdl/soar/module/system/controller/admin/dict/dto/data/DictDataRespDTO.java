package com.hdl.soar.module.system.controller.admin.dict.dto.data;

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
@Schema(description = "Admin backend - Dictionary Data Response VO")
public class DictDataRespDTO {

    @Schema(description = "Dictionary data ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Dictionary ID")
    private Long id;

    @Schema(description = "Display order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Dictionary sort order")
    private Integer sort;

    @Schema(description = "Dictionary label", requiredMode = Schema.RequiredMode.REQUIRED, example = "Soar")
    @ExcelProperty("Dictionary label")
    private String label;

    @Schema(description = "Dictionary value", requiredMode = Schema.RequiredMode.REQUIRED, example = "hdl")
    @ExcelProperty("Dictionary value")
    private String value;

    @Schema(description = "Dictionary type", requiredMode = Schema.RequiredMode.REQUIRED, example = "common_status")
    @ExcelProperty("Dictionary type")
    private String dictType;

    @Schema(description = "Status, see CommonStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "Status", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "Color type: default, primary, success, info, warning, danger", example = "default")
    private String colorType;

    @Schema(description = "CSS class", example = "btn-visible")
    private String cssClass;

    @Schema(description = "Remark", example = "I am a role")
    private String remark;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED, example = "timestamp format")
    private Instant createTime;

}
