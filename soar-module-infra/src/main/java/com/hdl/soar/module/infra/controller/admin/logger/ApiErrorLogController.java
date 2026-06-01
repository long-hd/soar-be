package com.hdl.soar.module.infra.controller.admin.logger;

import com.hdl.soar.framework.apilog.core.annotation.ApiAccessLog;
import com.hdl.soar.framework.common.enums.OperateTypeEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.infra.controller.admin.logger.dto.apierrorlog.ApiErrorLogPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.logger.dto.apierrorlog.ApiErrorLogRespDTO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiErrorLogPO;
import com.hdl.soar.module.infra.mapper.logger.ApiErrorLogMapper;
import com.hdl.soar.module.infra.service.logger.ApiErrorLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;
import static com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;


@Tag(name = "Admin Backend - API Error Log")
@Slf4j
@Validated
@RestController
@RequestMapping("/infra/api-error-log")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiErrorLogController {

    ApiErrorLogService apiErrorLogService;

    @GetMapping("/get")
    @Operation(summary = "Get API error log detail")
    @Parameter(name = "id", description = "Log ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:query')")
    public CommonResult<ApiErrorLogRespDTO> getApiErrorLog(@RequestParam("id") Long id) {
        ApiErrorLogPO po = apiErrorLogService.getApiErrorLog(id);
        return success(ApiErrorLogMapper.INSTANCE.toDTO(po));
    }

    @GetMapping("/page")
    @Operation(summary = "Get API error log page")
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:query')")
    public CommonResult<PageResult<ApiErrorLogRespDTO>> getApiErrorLogPage(
            @Valid ApiErrorLogPageReqDTO pageReqDTO) {
        PageResult<ApiErrorLogPO> pageResult = apiErrorLogService.getApiErrorLogPage(pageReqDTO);
        return success(new PageResult<>(
                ApiErrorLogMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()
        ));
    }

    @PutMapping("/update-status")
    @Operation(summary = "Update API error log processing status")
    @Parameters({
            @Parameter(name = "id", description = "Log ID", required = true, example = "1024"),
            @Parameter(name = "processStatus", description = "Processing status", required = true, example = "1")
    })
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:update-status')")
    public CommonResult<Boolean> updateApiErrorLogProcess(
            @RequestParam("id") Long id,
            @RequestParam("processStatus") Integer processStatus) {
        apiErrorLogService.updateApiErrorLogProcess(id, processStatus, getLoginUserId());
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export API error logs")
    @ApiResponse(content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportApiErrorLog(HttpServletResponse response,
                                  @Valid ApiErrorLogPageReqDTO exportReqDTO) throws IOException {
        exportReqDTO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ApiErrorLogPO> list = apiErrorLogService.getApiErrorLogPage(exportReqDTO).getList();
        ExcelUtils.write(response,
                "api-error-logs.xlsx",
                "API Error Logs",
                ApiErrorLogRespDTO.class,
                ApiErrorLogMapper.INSTANCE.toDTOList(list));
    }


}
