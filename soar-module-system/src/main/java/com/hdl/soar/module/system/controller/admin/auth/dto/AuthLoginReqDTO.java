package com.hdl.soar.module.system.controller.admin.auth.dto;

import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.validation.InEnum;
import com.hdl.soar.module.system.enums.SocialTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin Backend - Username and Password Login Request DTO. " +
        "If logging in and binding a social user, parameters starting with 'social' must be provided")
public class AuthLoginReqDTO extends CaptchaVerificationReqDTO {

    @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED, example = "Bob")
    @NotBlank(message = "Login username cannot be empty")
    @Size(min = 4, max = 30, message = "Username length must be between 4 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,30}$", message = "Username format must contain only letters and numbers")
    private String username;

    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED, example = "Bob123")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 4, max = 16, message = "Password length must be between 4 and 16 characters")
    private String password;

    // ========== The following parameters are required when binding social login ==========

    @Schema(description = "Social platform type, see values in SocialTypeEnum", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @InEnum(SocialTypeEnum.class)
    private Integer socialType;

    @Schema(description = "Authorization code", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String socialCode;

    @Schema(description = "State", requiredMode = Schema.RequiredMode.REQUIRED, example = "9b2ffbc1-7425-4155-9894-9d5c08541d62")
    private String socialState;

    @AssertTrue(message = "Authorization code cannot be empty")
    public boolean isSocialCodeValid() {
        return socialType == null || StrUtil.isNotEmpty(socialCode);
    }

    @AssertTrue(message = "Authorization state cannot be empty")
    public boolean isSocialState() {
        return socialType == null || StrUtil.isNotEmpty(socialState);
    }

}
