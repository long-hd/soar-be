package com.hdl.soar.module.system.controller.admin.user.dto.user;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin Backend - User Page Request DTO")
public class UserPageReqDTO extends PageParam {

    @Schema(description = "Username (fuzzy match)", example = "admin")
    private String username;

    @Schema(description = "Mobile (fuzzy match)", example = "1560")
    private String mobile;

    @Schema(description = "Status: 0=Enabled, 1=Disabled", example = "0")
    private Integer status;

    @Schema(description = "Department ID (includes child departments)", example = "1")
    private Long deptId;

    @Schema(description = "Create time start")
    private Instant createTimeStart;

    @Schema(description = "Create time end")
    private Instant createTimeEnd;

}
