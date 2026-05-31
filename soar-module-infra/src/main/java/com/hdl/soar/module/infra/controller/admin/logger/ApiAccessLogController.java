package com.hdl.soar.module.infra.controller.admin.logger;

import com.hdl.soar.framework.apilog.core.annotation.ApiAccessLog;
import com.hdl.soar.framework.common.enums.OperateTypeEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.infra.controller.admin.logger.dto.ApiAccessLogPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.logger.dto.ApiAccessLogRespDTO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiAccessLogPO;
import com.hdl.soar.module.infra.mapper.logger.ApiAccessLogMapper;
import com.hdl.soar.module.infra.service.logger.ApiAccessLogService;
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

@Tag(name = "Admin Backend - API Access Log")
@Slf4j
@Validated
@RestController
@RequestMapping("/infra/api-access-log")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiAccessLogController {

    ApiAccessLogService apiAccessLogService;

    @GetMapping("/get")
    @Operation(summary = "Get API access log detail")
    @Parameter(name = "id", description = "Log ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:query')")
    public CommonResult<ApiAccessLogRespDTO> getApiAccessLog(@RequestParam("id") Long id) {
        ApiAccessLogPO po = apiAccessLogService.getApiAccessLog(id);
        return success(ApiAccessLogMapper.INSTANCE.toDTO(po));
    }

    @GetMapping("/page")
    @Operation(summary = "Get API access log page")
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:query')")
    public CommonResult<PageResult<ApiAccessLogRespDTO>> getApiAccessLogPage(
            @Valid ApiAccessLogPageReqDTO pageReqDTO) {
        PageResult<ApiAccessLogPO> pageResult = apiAccessLogService.getApiAccessLogPage(pageReqDTO);
        return success(new PageResult<>(
                ApiAccessLogMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()
        ));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export API access logs")
    @ApiResponse(content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportApiAccessLog(HttpServletResponse response,
                                   @Valid ApiAccessLogPageReqDTO exportReqDTO) throws IOException {
        exportReqDTO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ApiAccessLogPO> list = apiAccessLogService.getApiAccessLogPage(exportReqDTO).getList();
        ExcelUtils.write(response,
                "api-access-logs.xlsx",
                "API Access Logs",
                ApiAccessLogRespDTO.class,
                ApiAccessLogMapper.INSTANCE.toDTOList(list));
    }


}
