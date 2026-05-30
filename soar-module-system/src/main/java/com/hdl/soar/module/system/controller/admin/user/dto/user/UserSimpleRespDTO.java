package com.hdl.soar.module.system.controller.admin.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - User Simple Response DTO")
public class UserSimpleRespDTO {

    @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "Nickname", requiredMode = Schema.RequiredMode.REQUIRED, example = "HDL")
    private String nickname;

    @Schema(description = "Department ID", example = "1")
    private Long deptId;
    @Schema(description = "Department name", example = "Engineering")
    private String deptName;

}
