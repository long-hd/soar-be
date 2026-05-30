package com.hdl.soar.module.system.controller.admin.dept.dto.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Department Create/Update Request DTO")
public class DeptSaveReqDTO {

    @Schema(description = "Department ID (null for create)", example = "1024")
    private Long id;

    @Schema(description = "Department name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Engineering")
    @NotBlank(message = "Department name cannot be blank")
    @Size(max = 30, message = "Department name must not exceed 30 characters")
    private String name;

    @Schema(description = "Parent department ID", example = "0")
    private Long parentId;

    @Schema(description = "Display sort order", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Sort order cannot be null")
    private Integer sort;

    @Schema(description = "Leader user ID", example = "1")
    private Long leaderUserId;

    @Schema(description = "Contact phone", example = "0123456789")
    @Size(max = 11, message = "Phone must not exceed 11 characters")
    private String phone;

    @Schema(description = "Email", example = "dept@example.com")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @Schema(description = "Status: 0=Enabled, 1=Disabled", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "Status cannot be null")
    private Integer status;

}