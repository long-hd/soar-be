package com.hdl.soar.module.system.controller.admin.user.dto.user;

import cn.idev.excel.annotation.ExcelProperty;
import com.hdl.soar.framework.excel.core.annotations.DictFormat;
import com.hdl.soar.framework.excel.core.annotations.ExcelColumnSelect;
import com.hdl.soar.framework.excel.core.convert.DictConvert;
import com.hdl.soar.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Admin Backend - User Import Excel DTO")
public class UserImportExcelDTO {

    @ExcelProperty("Username")
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, max = 30)
    private String username;

    @ExcelProperty("Nickname")
    @Size(max = 30)
    private String nickname;

    @ExcelProperty("Department ID")
    private Long deptId;

    @ExcelProperty("Email")
    @Email(message = "Invalid email format")
    @Size(max = 50)
    private String email;

    @ExcelProperty("Mobile")
    @Size(max = 11)
    private String mobile;

    @ExcelProperty(value = "Sex", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.USER_SEX)
    @ExcelColumnSelect(dictType = DictTypeConstants.USER_SEX)
    private Integer sex;

    @ExcelProperty(value = "Status", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    @ExcelColumnSelect(dictType = DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
