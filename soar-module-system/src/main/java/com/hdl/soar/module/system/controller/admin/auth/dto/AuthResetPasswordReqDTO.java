package com.hdl.soar.module.system.controller.admin.auth.dto;

import com.hdl.soar.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin Backend - SMS Reset Account Password Request VO")
public class AuthResetPasswordReqDTO {

    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 4, max = 16, message = "Password length must be between 4 and 16 characters")
    private String password;

    @Schema(description = "Mobile number", requiredMode = Schema.RequiredMode.REQUIRED, example = "13312341234")
    @NotBlank(message = "Mobile number cannot be empty")
    @Mobile
    private String mobile;

    @Schema(description = "SMS verification code", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "SMS verification code cannot be empty")
    private String code;

}