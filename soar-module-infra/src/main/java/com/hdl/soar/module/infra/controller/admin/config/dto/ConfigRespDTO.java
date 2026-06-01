package com.hdl.soar.module.infra.controller.admin.config.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.hdl.soar.framework.excel.core.annotations.DictFormat;
import com.hdl.soar.framework.excel.core.convert.DictConvert;
import com.hdl.soar.module.infra.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@ExcelIgnoreUnannotated
@Schema(description = "Admin Backend - Config Response DTO")
public class ConfigRespDTO {

    @Schema(description = "Config ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("Config ID")
    private Long id;

    @Schema(description = "Config category", requiredMode = Schema.RequiredMode.REQUIRED, example = "biz")
    @ExcelProperty("Category")
    private String category;

    @Schema(description = "Config name", requiredMode = Schema.RequiredMode.REQUIRED, example = "User initial password")
    @ExcelProperty("Name")
    private String name;

    @Schema(description = "Config key", requiredMode = Schema.RequiredMode.REQUIRED, example = "system.user.init-password")
    @ExcelProperty("Key")
    private String key;

    @Schema(description = "Config value", example = "123456")
    @ExcelProperty("Value")
    private String value;

    @Schema(description = "Config type", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "Type", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.CONFIG_TYPE)
    private Integer type;

    @Schema(description = "Visible to frontend", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty(value = "Visible", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BOOLEAN_STRING)
    private Boolean visible;

    @Schema(description = "Remark", example = "Default password for new users")
    @ExcelProperty("Remark")
    private String remark;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Creation Time")
    private Instant createTime;

}
