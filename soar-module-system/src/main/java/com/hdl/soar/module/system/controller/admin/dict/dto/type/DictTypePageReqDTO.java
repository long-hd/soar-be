package com.hdl.soar.module.system.controller.admin.dict.dto.type;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin backend - Dictionary Type Page Request VO")
public class DictTypePageReqDTO extends PageParam {

    @Schema(description = "Dictionary type name, fuzzy match", example = "example")
    private String name;

    @Schema(description = "Dictionary type, fuzzy match", example = "common_status")
    @Size(max = 100, message = "Dictionary type length cannot exceed 100 characters")
    private String type;

    @Schema(description = "Display status, see CommonStatusEnum enum", example = "0")
    private Integer status;

    @Schema(description = "Create time", example = "[2022-07-01T00:00:00Z, 2022-07-01T00:00:00Z]")
    @Size(min = 2, max = 2)
    private Instant[] createTime;

}