package com.hdl.soar.module.system.controller.admin.user.dto.profile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - User Profile Change Password Request DTO")
public class UserProfileUpdatePasswordReqDTO {

    @Schema(description = "Old password", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "Old password cannot be blank")
    @Size(min = 4, max = 100, message = "Password must be 4-100 characters")
    @JsonDeserialize(using = StringDeserializer.class)
    private String oldPassword;

    @Schema(description = "New password", requiredMode = Schema.RequiredMode.REQUIRED, example = "654321")
    @NotBlank(message = "New password cannot be blank")
    @Size(min = 4, max = 100, message = "Password must be 4-100 characters")
    @JsonDeserialize(using = StringDeserializer.class)
    private String newPassword;

}