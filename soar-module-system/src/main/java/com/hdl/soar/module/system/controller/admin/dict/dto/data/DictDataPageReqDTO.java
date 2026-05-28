package com.hdl.soar.module.system.controller.admin.dict.dto.data;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin backend - Dictionary Type Pagination Request DTO")
public class DictDataPageReqDTO extends PageParam {

    @Schema(description = "Dictionary label", example = "Soar")
    @Size(max = 100, message = "Dictionary label length cannot exceed 100 characters")
    private String label;

    @Schema(description = "Dictionary type (fuzzy match)", example = "sys_common_status")
    @Size(max = 100, message = "Dictionary type length cannot exceed 100 characters")
    private String dictType;

    @Schema(description = "Display status, see CommonStatusEnum", example = "1")
    @InEnum(value = CommonStatusEnum.class, message = "Status must be one of {value}")
    private Integer status;

}
