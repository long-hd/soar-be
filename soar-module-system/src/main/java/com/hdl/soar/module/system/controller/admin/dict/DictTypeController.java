package com.hdl.soar.module.system.controller.admin.dict;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypePageReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeRespDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictTypePO;
import com.hdl.soar.module.system.mapper.dict.DictTypeMapper;
import com.hdl.soar.module.system.service.dict.DictTypeService;
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

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Dictionary Type")
@Slf4j
@Validated
@RestController
@RequestMapping("/system/dict-type")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DictTypeController {

    DictTypeService dictTypeService;

    @PostMapping("/create")
    @Operation(summary = "Create Dictionary Type")
    @PreAuthorize("@ss.hasPermission('system:dict:create')")
    public CommonResult<Long> createDictType(@Valid @RequestBody DictTypeSaveReqDTO createReqDTO) {
        Long dictTypeId = dictTypeService.createDictType(createReqDTO);
        return success(dictTypeId);
    }

    @PutMapping("/update")
    @Operation(summary = "Update Dictionary Type")
    @PreAuthorize("@ss.hasPermission('system:dict:update')")
    public CommonResult<Boolean> updateDictType(@Valid @RequestBody DictTypeSaveReqDTO updateReqDTO) {
        dictTypeService.updateDictType(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete dictionary type")
    @Parameter(name = "id", description = "ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> deleteDictType(Long id) {
        dictTypeService.deleteDictType(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "Batch delete dictionary types")
    @Parameter(name = "ids", description = "List of IDs", required = true)
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> deleteDictTypeList(@RequestParam("ids") List<Long> ids) {
        dictTypeService.deleteDictTypeList(ids);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "Get paginated list of dictionary types")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<PageResult<DictTypeRespDTO>> pageDictTypes(@Valid DictTypePageReqDTO pageReqDTO) {
        PageResult<DictTypePO> pageResult = dictTypeService.getDictTypePage(pageReqDTO);
        return success(new PageResult<>(
                DictTypeMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()
        ));
    }

    @Operation(summary = "Get dictionary type details")
    @Parameter(name = "id", description = "ID", required = true, example = "1024")
    @GetMapping(value = "/get")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<DictTypeRespDTO> getDictType(@RequestParam("id") Long id) {
        DictTypePO dictType = dictTypeService.getDictType(id);
        return success(DictTypeMapper.INSTANCE.toDTO(dictType));
    }

    @GetMapping(value = {"/list-all-simple", "simple-list"})
    @Operation(
            summary = "Get all dictionary type list",
            description = "Includes enabled and disabled dictionary types, mainly used for frontend dropdown options"
    ) // No permission required because it is used globally in frontend
    public CommonResult<List<DictTypeSimpleRespDTO>> getSimpleDictTypeList() {
        List<DictTypePO> list = dictTypeService.getDictTypeList();
        return success(DictTypeMapper.INSTANCE.toSimpleDTOList(list));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export Dictionary Types")
    @ApiResponse(content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public void export(HttpServletResponse response,
                       @Valid DictTypePageReqDTO exportReqDTO) throws IOException {
        exportReqDTO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DictTypePO> list = dictTypeService.getDictTypePage(exportReqDTO).getList();
        ExcelUtils.write(response, "dictionary-types.xlsx", "Dictionary Types", DictTypeRespDTO.class,
                DictTypeMapper.INSTANCE.toDTOList(list));
    }

}
