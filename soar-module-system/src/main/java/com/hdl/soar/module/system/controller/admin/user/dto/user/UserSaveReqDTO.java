package com.hdl.soar.module.system.controller.admin.user.dto.user;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Admin Backend - User Create/Update Request DTO")
public class UserSaveReqDTO {

    @Schema(description = "User ID (null for create)", example = "1")
    private Long id;

    @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, max = 30, message = "Username must be 4-30 characters")
    // @DiffLogField(name = "Username")
    private String username;

    @Schema(description = "Nickname", requiredMode = Schema.RequiredMode.REQUIRED, example = "HDL")
    @Size(max = 30, message = "Nickname length cannot exceed 30 characters")
    // @DiffLogField(name = "Nickname")
    private String nickname;

    @Schema(description = "Remark", example = "I am a user")
    // @DiffLogField(name = "Remark")
    private String remark;

    @Schema(description = "Department ID", example = "1")
    // @DiffLogField(name = "Department", function = DeptParseFunction.NAME)
    private Long deptId;

    @Schema(description = "Position IDs", example = "1")
    // @DiffLogField(name = "Position", function = PostParseFunction.NAME)
    private Set<Long> postIds;

    @Schema(description = "Email", example = "user@example.com")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    // @DiffLogField(name = "Email")
    private String email;

    @Schema(description = "Mobile", example = "0912345678")
    @Size(max = 11, message = "Mobile must not exceed 11 characters")
    // @DiffLogField(name = "Mobile number")
    private String mobile;

    @Schema(description = "Gender, see SexEnum: 1=Male, 2=Female, 3=Unknown", example = "1")
    // @DiffLogField(name = "Gender", function = SexParseFunction.NAME)
    private Integer sex;

    @Schema(description = "Avatar URL", example = "https://www.google.com/xxx.png")
    // @DiffLogField(name = "Avatar")
    private String avatar;

    // ========== Fields required only when creating a user ==========

    @Schema(description = "Password (required for create, ignored for update)", example = "123456")
    @Size(min = 4, max = 100, message = "Password must be 4-100 characters")
    @JsonDeserialize(using = StringDeserializer.class) // Exclude from global StringTrimDeserializer
    private String password;

    @AssertTrue(message = "Password cannot be empty")
    @JsonIgnore
    public boolean isPasswordValid() {
        return id != null // Not required when updating
                || (ObjectUtil.isAllNotEmpty(password)); // Required when creating
    }

}
