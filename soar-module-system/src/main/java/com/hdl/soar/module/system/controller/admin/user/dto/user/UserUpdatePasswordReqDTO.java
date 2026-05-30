package com.hdl.soar.module.system.controller.admin.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - User Reset Password Request DTO")
public class UserUpdatePasswordReqDTO {

    @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "User ID cannot be null")
    private Long id;

    @Schema(description = "New password", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 4, max = 100, message = "Password must be 4-100 characters")
    private String password;

}
