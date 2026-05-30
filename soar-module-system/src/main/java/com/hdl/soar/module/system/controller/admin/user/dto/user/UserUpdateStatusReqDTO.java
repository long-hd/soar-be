package com.hdl.soar.module.system.controller.admin.user.dto.user;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.validation.InDict;
import com.hdl.soar.framework.common.validation.InEnum;
import com.hdl.soar.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - User Update Status Request DTO")
public class UserUpdateStatusReqDTO {

    @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "User ID cannot be null")
    private Long id;

    @Schema(description = "Status: 0=Enabled, 1=Disabled", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "Status cannot be null")
    @InEnum(value = CommonStatusEnum.class, message = "The updated status must be one of {value}")
    @InDict(type = DictTypeConstants.COMMON_STATUS)
    private Integer status;

}