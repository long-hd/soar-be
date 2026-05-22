package com.hdl.soar.module.system.controller.admin.logger.dto.loginlog;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Schema(description = "Admin Backend - Login Log Paginated List Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogPageReqDTO extends PageParam {

    @Schema(description = "User IP, fuzzy match", example = "127.0.0.1")
    private String userIp;

    @Schema(description = "Username, fuzzy match", example = "Bob")
    private String username;

    @Schema(description = "Operation status", example = "true")
    private Boolean status;

    @Schema(description = "Login time range", example = "[2022-07-01T00:00:00Z,2022-07-01T23:59:59Z]")
    private Instant[] createTime;

}
