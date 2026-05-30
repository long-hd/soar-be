package com.hdl.soar.module.system.controller.admin.dept.dto.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin Backend - Department Response DTO")
public class DeptRespDTO {

    @Schema(description = "Department ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Department name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Engineering")
    private String name;

    @Schema(description = "Parent department ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long parentId;

    @Schema(description = "Display sort order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "Leader user ID", example = "1")
    private Long leaderUserId;

    @Schema(description = "Contact phone", example = "0123456789")
    private String phone;

    @Schema(description = "Email", example = "dept@example.com")
    private String email;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "Creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant createTime;

}
