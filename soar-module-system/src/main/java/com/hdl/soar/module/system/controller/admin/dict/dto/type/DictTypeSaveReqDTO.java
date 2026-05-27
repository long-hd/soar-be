package com.hdl.soar.module.system.controller.admin.dict.dto.type;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Dictionary Type Create/Update Request DTO")
public class DictTypeSaveReqDTO {

    @Schema(description = "Dict type ID (null for create)", example = "1024")
    private Long id;

    @Schema(description = "Dictionary Name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Gender")
    @NotBlank(message = "Dictionary name cannot be empty")
    @Size(max = 100, message = "Dictionary type name length cannot exceed 100 characters")
    private String name;

    @Schema(description = "Dictionary Type", requiredMode = Schema.RequiredMode.REQUIRED, example = "sys_common_status")
    @NotBlank(message = "Dictionary type cannot be empty")
    @Size(max = 100, message = "Dictionary type length cannot exceed 100 characters")
    private String type;

    @Schema(description = "Status, see CommonStatusEnum enum", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Status cannot be empty")
    private Integer status;

    @Schema(description = "Remark", example = "Happy remark")
    private String remark;

}
