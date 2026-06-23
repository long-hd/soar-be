package com.hdl.soar.module.system.controller.admin.dept.dto.post;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin backend - Position create/update Request DTO")
public class PostSaveReqDTO {

    @Schema(description = "Position ID (Null on create)", example = "1024")
    private Long id;

    @Schema(description = "Position name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Junior Developer")
    @NotBlank(message = "Position name cannot be empty")
    @Size(max = 50, message = "Position name length cannot exceed 50 characters")
    private String name;

    @Schema(description = "Position code", requiredMode = Schema.RequiredMode.REQUIRED, example = "dev")
    @NotBlank(message = "Position code cannot be empty")
    @Size(max = 64, message = "Position code length cannot exceed 64 characters")
    private String code;

    @Schema(description = "Display order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Display order cannot be empty")
    private Integer sort;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "Remark", example = "Some notes")
    private String remark;
}
