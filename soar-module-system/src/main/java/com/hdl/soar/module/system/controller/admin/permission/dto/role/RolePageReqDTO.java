package com.hdl.soar.module.system.controller.admin.permission.dto.role;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin backend - Role page request DTO")
public class RolePageReqDTO extends PageParam {

    @Schema(description = "Role name, fuzzy match", example = "admin")
    private String name;

    @Schema(description = "Role code, fuzzy match", example = "admin")
    private String code;

    @Schema(description = "Status, see CommonStatusEnum", example = "1")
    private Integer status;

    @Schema(description = "Create time", example = "2022-07-01T00:00:00Z")
    private Instant createTimeStart;

    @Schema(description = "Create time", example = "2022-07-01T00:00:00Z")
    private Instant createTimeEnd;

}
