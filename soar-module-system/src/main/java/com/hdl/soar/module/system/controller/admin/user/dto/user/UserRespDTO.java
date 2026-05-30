package com.hdl.soar.module.system.controller.admin.user.dto.user;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.hdl.soar.framework.excel.core.annotations.DictFormat;
import com.hdl.soar.framework.excel.core.convert.DictConvert;
import com.hdl.soar.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@Schema(description = "Admin Backend - User Response DTO")
@ExcelIgnoreUnannotated
public class UserRespDTO {

    @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("User ID")
    private Long id;

    @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @ExcelProperty("Username")
    private String username;

    @Schema(description = "Nickname", example = "Long")
    @ExcelProperty("Nickname")
    private String nickname;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Department ID", example = "1")
    private Long deptId;
    @Schema(description = "Department name", example = "Engineering")
    @ExcelProperty("Department")
    private String deptName;

    @Schema(description = "Post IDs", example = "[1, 2]")
    private Set<Long> postIds;

    @Schema(description = "Email", example = "user@example.com")
    @ExcelProperty("Email")
    private String email;

    @Schema(description = "Mobile", example = "0912345678")
    @ExcelProperty("Mobile")
    private String mobile;

    @Schema(description = "Sex: 1=Male, 2=Female, 3=Unknown", example = "1")
    @ExcelProperty(value = "Sex", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.USER_SEX)
    private Integer sex;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "Status", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "Last login IP", example = "192.168.1.1")
    @ExcelProperty("Last Login IP")
    private String loginIp;

    @Schema(description = "Last login time")
    @ExcelProperty("Last Login Time")
    private Instant loginDate;

    @Schema(description = "Creation time")
    private Instant createTime;

}
