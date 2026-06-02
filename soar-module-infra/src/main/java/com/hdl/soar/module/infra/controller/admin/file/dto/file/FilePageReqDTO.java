package com.hdl.soar.module.infra.controller.admin.file.dto.file;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin Backend - File Page Request DTO")
public class FilePageReqDTO extends PageParam {

    @Schema(description = "File name (fuzzy match)")
    private String name;

    @Schema(description = "File path (fuzzy match)")
    private String path;

    @Schema(description = "Content type (fuzzy match)")
    private String type;

    @Schema(description = "Creation time range")
    private Instant[] createTime;

}