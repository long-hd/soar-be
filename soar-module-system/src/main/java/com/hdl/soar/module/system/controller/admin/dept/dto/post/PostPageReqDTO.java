package com.hdl.soar.module.system.controller.admin.dept.dto.post;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin backend - Position pagination Request VO")
public class PostPageReqDTO extends PageParam {

    @Schema(description = "Position code, fuzzy match", example = "hdl")
    private String code;

    @Schema(description = "Position name, fuzzy match", example = "Soar")
    private String name;

    @Schema(description = "Display status, see CommonStatusEnum", example = "1")
    private Integer status;

}
