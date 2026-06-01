package com.hdl.soar.module.infra.controller.admin.config.dto;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin Backend - Config Page Request DTO")
public class ConfigPageReqDTO extends PageParam {

    @Schema(description = "Config name (fuzzy match)", example = "password")
    private String name;

    @Schema(description = "Config key (fuzzy match)", example = "system.user")
    private String key;

    @Schema(description = "Config type: 1=System, 2=Custom", example = "1")
    private Integer type;

    @Schema(description = "Creation time range", example = "[2024-01-01T00:00:00Z,2024-01-31T23:59:59Z]")
    private Instant[] createTime;

}
