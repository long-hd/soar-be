package com.hdl.soar.module.system.controller.admin.user.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - User Profile Update Request DTO")
public class UserProfileUpdateReqDTO {

    @Schema(description = "Nickname", example = "Long")
    @Size(max = 30, message = "Nickname length cannot exceed 30 characters")
    private String nickname;

    @Schema(description = "Email", example = "user@example.com")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @Schema(description = "Mobile", example = "0912345678")
    @Size(max = 11, message = "Mobile must not exceed 11 characters")
    private String mobile;

    @Schema(description = "Sex: 1=Male, 2=Female, 3=Unknown", example = "1")
    private Integer sex;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.png")
    private String avatar;

}
