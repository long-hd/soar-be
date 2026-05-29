package com.hdl.soar.module.system.controller.admin.permission.dto.role;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Admin backend - Role create/update request VO
 */
@Data
@Schema(description = "Admin backend - Role create/update request DTO")
public class RoleSaveReqDTO {

    @Schema(description = "Role ID (null for create)", example = "1")
    private Long id;

    @Schema(description = "Role name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Administrator")
    @NotBlank(message = "Role name cannot be empty")
    @Size(max = 30, message = "Role name length cannot exceed 30 characters")
    private String name;

    @NotBlank(message = "Role code cannot be empty")
    @Size(max = 100, message = "Role code length cannot exceed 100 characters")
    @Schema(description = "Role code", requiredMode = Schema.RequiredMode.REQUIRED, example = "ADMIN")
    private String code;

    @Schema(description = "Display order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Display order cannot be empty")
    private Integer sort;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "Status cannot be empty")
    @InEnum(value = CommonStatusEnum.class, message = "Status must be {value}")
    private Integer status;

    @Schema(description = "Remark", example = "I am a role")
    @Size(max = 500, message = "Remark length cannot exceed 500 characters")
    private String remark;

}
