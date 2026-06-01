package com.hdl.soar.module.infra.controller.admin.config;
import com.hdl.soar.framework.apilog.core.annotation.ApiAccessLog;
import com.hdl.soar.framework.common.enums.OperateTypeEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.infra.controller.admin.config.dto.ConfigPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.config.dto.ConfigRespDTO;
import com.hdl.soar.module.infra.controller.admin.config.dto.ConfigSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.config.ConfigPO;
import com.hdl.soar.module.infra.mapper.config.ConfigMapper;
import com.hdl.soar.module.infra.service.config.ConfigService;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.common.pojo.CommonResult.success;
import static com.hdl.soar.module.infra.enums.ErrorCodeConstants.CONFIG_GET_VALUE_ERROR_IF_VISIBLE;

@Tag(name = "Admin Backend - Config")
@Slf4j
@Validated
@RestController
@RequestMapping("/infra/config")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigController {

    ConfigService configService;

    @PostMapping("/create")
    @Operation(summary = "Create config")
    @PreAuthorize("@ss.hasPermission('infra:config:create')")
    public CommonResult<Long> createConfig(@Valid @RequestBody ConfigSaveReqDTO createReqDTO) {
        return success(configService.createConfig(createReqDTO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update config")
    @PreAuthorize("@ss.hasPermission('infra:config:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody ConfigSaveReqDTO updateReqDTO) {
        configService.updateConfig(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete config")
    @Parameter(name = "id", description = "Config ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:config:delete')")
    public CommonResult<Boolean> deleteConfig(@RequestParam("id") Long id) {
        configService.deleteConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get config detail")
    @Parameter(name = "id", description = "Config ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:config:query')")
    public CommonResult<ConfigRespDTO> getConfig(@RequestParam("id") Long id) {
        ConfigPO config = configService.getConfig(id);
        return success(ConfigMapper.INSTANCE.toDTO(config));
    }

    @GetMapping("/get-value-by-key")
    @Operation(summary = "Get config value by key",
            description = "Public API for frontend. Invisible configs are protected.")
    @Parameter(name = "key", description = "Config key", required = true, example = "system.user.init-password")
    public CommonResult<String> getConfigValueByKey(@RequestParam("key") String key) {
        ConfigPO config = configService.getConfigByKey(key);
        if (config == null) {
            return success(null);
        }
        if (!config.getVisible()) {
            throw exception(CONFIG_GET_VALUE_ERROR_IF_VISIBLE);
        }
        return success(config.getValue());
    }

    @GetMapping("/page")
    @Operation(summary = "Get config page")
    @PreAuthorize("@ss.hasPermission('infra:config:query')")
    public CommonResult<PageResult<ConfigRespDTO>> getConfigPage(@Valid ConfigPageReqDTO pageReqDTO) {
        PageResult<ConfigPO> pageResult = configService.getConfigPage(pageReqDTO);
        return success(new PageResult<>(
                ConfigMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()
        ));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export configs")
    @ApiResponse(content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    @PreAuthorize("@ss.hasPermission('infra:config:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportConfig(HttpServletResponse response,
                             @Valid ConfigPageReqDTO exportReqDTO) throws IOException {
        exportReqDTO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ConfigPO> list = configService.getConfigPage(exportReqDTO).getList();
        ExcelUtils.write(response, "configs.xlsx", "Configs",
                ConfigRespDTO.class,
                ConfigMapper.INSTANCE.toDTOList(list));
    }

}