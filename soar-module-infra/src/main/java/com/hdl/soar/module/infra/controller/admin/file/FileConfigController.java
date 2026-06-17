package com.hdl.soar.module.infra.controller.admin.file;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.module.infra.controller.admin.file.dto.config.FileConfigPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.config.FileConfigRespDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.config.FileConfigSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.file.FileConfigPO;
import com.hdl.soar.module.infra.mapper.file.FileConfigMapper;
import com.hdl.soar.module.infra.service.file.FileConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - File Config")
@Validated
@RestController
@RequestMapping("/infra/file-config")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileConfigController {

    FileConfigService fileConfigService;

    @PostMapping("/create")
    @Operation(summary = "Create file config")
    @PreAuthorize("@ss.hasPermission('infra:file-config:create')")
    public CommonResult<Long> createFileConfig(@Valid @RequestBody FileConfigSaveReqDTO createReqDTO) {
        return success(fileConfigService.createFileConfig(createReqDTO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update file config")
    @PreAuthorize("@ss.hasPermission('infra:file-config:update')")
    public CommonResult<Boolean> updateFileConfig(@Valid @RequestBody FileConfigSaveReqDTO updateReqDTO) {
        fileConfigService.updateFileConfig(updateReqDTO);
        return success(true);
    }

    @PutMapping("/update-master")
    @Operation(summary = "Set a file config as master (default)")
    @Parameter(name = "id", description = "Config ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:file-config:update')")
    public CommonResult<Boolean> updateFileConfigMaster(@RequestParam("id") Long id) {
        fileConfigService.updateFileConfigMaster(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete file config")
    @Parameter(name = "id", description = "Config ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:file-config:delete')")
    public CommonResult<Boolean> deleteFileConfig(@RequestParam("id") Long id) {
        fileConfigService.deleteFileConfig(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "Bulk delete file configs")
    @Parameter(name = "ids", description = "Config IDs (comma-separated)", required = true)
    @PreAuthorize("@ss.hasPermission('infra:file-config:delete')")
    public CommonResult<Boolean> deleteFileConfigList(@RequestParam("ids") List<Long> ids) {
        fileConfigService.deleteFileConfigList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get file config detail")
    @Parameter(name = "id", description = "Config ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:file-config:query')")
    public CommonResult<FileConfigRespDTO> getFileConfig(@RequestParam("id") Long id) {
        FileConfigPO po = fileConfigService.getFileConfig(id);
        return success(toRespDTO(po));
    }

    @GetMapping("/page")
    @Operation(summary = "Get file config page")
    @PreAuthorize("@ss.hasPermission('infra:file-config:query')")
    public CommonResult<PageResult<FileConfigRespDTO>> getFileConfigPage(@Valid FileConfigPageReqDTO pageReqDTO) {
        PageResult<FileConfigPO> pageResult = fileConfigService.getFileConfigPage(pageReqDTO);
        PageResult<FileConfigRespDTO> result = new PageResult<>(
                pageResult.getList().stream().map(this::toRespDTO).toList(),
                pageResult.getTotal());
        return success(result);
    }

    @GetMapping("/test")
    @Operation(summary = "Test a file config by uploading a sample file")
    @Parameter(name = "id", description = "Config ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:file-config:query')")
    public CommonResult<String> testFileConfig(@RequestParam("id") Long id) throws Exception {
        return success(fileConfigService.testFileConfig(id));
    }

    /**
     * Map PO to RespDTO, parsing the JSON {@code config} string back into a map for the FE.
     */
    private FileConfigRespDTO toRespDTO(FileConfigPO po) {
        if (po == null) {
            return null;
        }
        FileConfigRespDTO dto = FileConfigMapper.INSTANCE.toDTO(po);
        // config is ignored by MapStruct (Map<->String); parse the JSON here.
        dto.setConfig(JsonUtils.parseObject(po.getConfig(), Map.class));
        return dto;
    }

}
