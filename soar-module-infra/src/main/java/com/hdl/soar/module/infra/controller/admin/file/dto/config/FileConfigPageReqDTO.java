package com.hdl.soar.module.infra.controller.admin.file.dto.config;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin Backend - File Config Page Request DTO")
public class FileConfigPageReqDTO extends PageParam {

    @Schema(description = "Config name (fuzzy match)", example = "local")
    private String name;

    @Schema(description = "Storage type", example = "20")
    private Integer storage;

    @Schema(description = "Creation time range")
    private Instant[] createTime;

}