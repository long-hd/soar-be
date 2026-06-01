package com.hdl.soar.module.system.controller.admin.logger.dto.operatelog;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin Backend - Operate Log Page Request DTO")
public class OperateLogPageReqDTO extends PageParam {

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Module (fuzzy match)", example = "System User")
    private String module;

    @Schema(description = "Operation name (fuzzy match)", example = "Create")
    private String name;

    @Schema(description = "Business ID", example = "1024")
    private Long bizId;

    @Schema(description = "Content (fuzzy match)", example = "Created user")
    private String content;

    @Schema(description = "Creation time range", example = "[2024-01-01T00:00:00Z,2024-01-31T23:59:59Z]")
    private Instant[] createTime;

}
