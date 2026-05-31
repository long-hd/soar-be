package com.hdl.soar.module.system.controller.admin.logger;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.system.controller.admin.logger.dto.loginlog.LoginLogPageReqDTO;
import com.hdl.soar.module.system.controller.admin.logger.dto.loginlog.LoginLogRespDTO;
import com.hdl.soar.module.system.dal.entity.logger.LoginLogPO;
import com.hdl.soar.module.system.mapper.logger.LoginLogMapper;
import com.hdl.soar.module.system.service.logger.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Login Log")
@Slf4j
@Validated
@RestController
@RequestMapping("/system/login-log")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginLogController {

    LoginLogService loginLogService;

    @GetMapping("/get")
    @Operation(summary = "Get login log detail")
    @Parameter(name = "id", description = "Log ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:login-log:query')")
    public CommonResult<LoginLogRespDTO> getLoginLog(@RequestParam("id") Long id) {
        LoginLogPO loginLog = loginLogService.getLoginLog(id);
        return success(LoginLogMapper.INSTANCE.toDTO(loginLog));
    }

    @GetMapping("/page")
    @Operation(summary = "Get login log page")
    @PreAuthorize("@ss.hasPermission('system:login-log:query')")
    public CommonResult<PageResult<LoginLogRespDTO>> getLoginLogPage(
            @Valid LoginLogPageReqDTO pageReqDTO) {
        PageResult<LoginLogPO> pageResult = loginLogService.getLoginLogPage(pageReqDTO);
        return success(new PageResult<>(
                LoginLogMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()
        ));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export login logs")
    @ApiResponse(content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    @PreAuthorize("@ss.hasPermission('system:login-log:export')")
    // TODO @ApiAccessLog(operateType = EXPORT)
    public void exportLoginLog(HttpServletResponse response,
                               @Valid LoginLogPageReqDTO exportReqDTO) throws IOException {
        exportReqDTO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<LoginLogPO> list = loginLogService.getLoginLogPage(exportReqDTO).getList();
        ExcelUtils.write(response, "login-logs.xlsx", "Login Logs",
                LoginLogRespDTO.class,
                LoginLogMapper.INSTANCE.toDTOList(list));
    }

}
