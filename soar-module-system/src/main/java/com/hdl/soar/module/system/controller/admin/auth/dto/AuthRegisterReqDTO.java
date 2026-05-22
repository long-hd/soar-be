package com.hdl.soar.module.system.controller.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Register Request VO")
public class AuthRegisterReqDTO extends CaptchaVerificationReqDTO {

    @Schema(description = "User account", requiredMode = Schema.RequiredMode.REQUIRED, example = "Bob")
    @NotBlank(message = "User account cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,30}$", message = "User account must consist of letters and numbers")
    @Size(min = 4, max = 30, message = "User account length must be between 4 and 30 characters")
    private String username;

    @Schema(description = "User nickname", requiredMode = Schema.RequiredMode.REQUIRED, example = "Bobi")
    @NotBlank(message = "User nickname cannot be empty")
    @Size(max = 30, message = "User nickname cannot exceed 30 characters")
    private String nickname;

    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 4, max = 16, message = "Password length must be between 4 and 16 characters")
    private String password;

}
