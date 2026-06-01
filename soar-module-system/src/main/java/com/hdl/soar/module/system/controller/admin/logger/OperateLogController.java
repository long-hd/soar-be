package com.hdl.soar.module.system.controller.admin.logger;

import com.hdl.soar.framework.apilog.core.annotation.ApiAccessLog;
import com.hdl.soar.framework.common.enums.OperateTypeEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.system.controller.admin.logger.dto.operatelog.OperateLogPageReqDTO;
import com.hdl.soar.module.system.controller.admin.logger.dto.operatelog.OperateLogRespDTO;
import com.hdl.soar.module.system.dal.entity.logger.OperateLogPO;
import com.hdl.soar.module.system.mapper.logger.OperateLogMapper;
import com.hdl.soar.module.system.service.logger.OperateLogService;
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

@Tag(name = "Admin Backend - Operate Log")
@Slf4j
@Validated
@RestController
@RequestMapping("/system/operate-log")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OperateLogController {

    OperateLogService operateLogService;

    @GetMapping("/get")
    @Operation(summary = "Get operate log detail")
    @Parameter(name = "id", description = "Log ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:operate-log:query')")
    public CommonResult<OperateLogRespDTO> getOperateLog(@RequestParam("id") Long id) {
        OperateLogPO po = operateLogService.getOperateLog(id);
        return success(OperateLogMapper.INSTANCE.toDTO(po));
    }

    @GetMapping("/page")
    @Operation(summary = "Get operate log page")
    @PreAuthorize("@ss.hasPermission('system:operate-log:query')")
    public CommonResult<PageResult<OperateLogRespDTO>> getOperateLogPage(
            @Valid OperateLogPageReqDTO pageReqDTO) {
        PageResult<OperateLogPO> pageResult = operateLogService.getOperateLogPage(pageReqDTO);
        return success(new PageResult<>(
                OperateLogMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()
        ));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export operate logs")
    @ApiResponse(content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    @PreAuthorize("@ss.hasPermission('system:operate-log:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportOperateLog(HttpServletResponse response,
                                 @Valid OperateLogPageReqDTO exportReqDTO) throws IOException {
        exportReqDTO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OperateLogPO> list = operateLogService.getOperateLogPage(exportReqDTO).getList();
        ExcelUtils.write(response,
                "operate-logs.xlsx",
                "Operate Logs",
                OperateLogRespDTO.class,
                OperateLogMapper.INSTANCE.toDTOList(list));
    }

}
