package com.hdl.soar.module.system.controller.admin.dict.dto.data;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin Dashboard - Dictionary Data Create/Update Request DTO")
public class DictDataSaveReqDTO {

    @Schema(description = "Dictionary Data ID", example = "1024")
    private Long id;

    @Schema(description = "Display Order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Display order cannot be null")
    private Integer sort;

    @Schema(description = "Dictionary Label", requiredMode = Schema.RequiredMode.REQUIRED, example = "Soar")
    @NotBlank(message = "Dictionary label cannot be blank")
    @Size(max = 100, message = "Dictionary label length cannot exceed 100 characters")
    private String label;

    @Schema(description = "Dictionary Value", requiredMode = Schema.RequiredMode.REQUIRED, example = "hdl")
    @NotBlank(message = "Dictionary value cannot be blank")
    @Size(max = 100, message = "Dictionary value length cannot exceed 100 characters")
    private String value;

    @Schema(description = "Dictionary Type", requiredMode = Schema.RequiredMode.REQUIRED, example = "sys_common_status")
    @NotBlank(message = "Dictionary type cannot be blank")
    @Size(max = 100, message = "Dictionary type length cannot exceed 100 characters")
    private String dictType;

    @Schema(description = "Status, see CommonStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Status cannot be null")
    @InEnum(value = CommonStatusEnum.class, message = "Status must be one of {value}")
    private Integer status;

    @Schema(description = "Color Type: default, primary, success, info, warning, danger", example = "default")
    private String colorType;

    @Schema(description = "CSS Class", example = "btn-visible")
    private String cssClass;

    @Schema(description = "Remark", example = "I am a role")
    private String remark;

}
