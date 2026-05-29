package com.hdl.soar.module.system.controller.admin.dict;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataPageReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataRespDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictDataPO;
import com.hdl.soar.module.system.mapper.dict.DictDataMapper;
import com.hdl.soar.module.system.service.dict.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Dashboard - Dictionary Data")
@Validated
@RestController
@RequestMapping("/system/dict-data")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DictDataController {

    DictDataService dictDataService;

    @PostMapping("/create")
    @Operation(summary = "Create Dictionary Data")
    @PreAuthorize("@ss.hasPermission('system:dict:create')")
    public CommonResult<Long> createDictData(@Valid @RequestBody DictDataSaveReqDTO createReqDTO) {
        Long dictDataId = dictDataService.createDictData(createReqDTO);
        return success(dictDataId);
    }

    @PutMapping("/update")
    @Operation(summary = "Update Dictionary Data")
    @PreAuthorize("@ss.hasPermission('system:dict:update')")
    public CommonResult<Boolean> updateDictData(@Valid @RequestBody DictDataSaveReqDTO updateReqVO) {
        dictDataService.updateDictData(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete Dictionary Data")
    @Parameter(name = "id", description = "ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> deleteDictData(@RequestParam("id") Long id) {
        dictDataService.deleteDictData(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "Batch Delete Dictionary Data")
    @Parameter(name = "ids", description = "ID List", required = true)
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> deleteDictDataList(@RequestParam("ids") List<Long> ids) {
        dictDataService.deleteDictDataList(ids);
        return success(true);
    }

    @GetMapping(value = {"/list-all-simple", "simple-list"})
    @Operation(summary = "Get All Dictionary Data List", description = "Typically used for admin frontend to cache dictionary data locally")
    public CommonResult<List<DictDataSimpleRespDTO>> getSimpleDictDataList() {
        List<DictDataPO> list = dictDataService.getDictDataList(CommonStatusEnum.ENABLE, null);
        return success(DictDataMapper.INSTANCE.toSimpleDTOList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "Get paginated dictionary types")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<PageResult<DictDataRespDTO>> getDictTypePage(@Valid DictDataPageReqDTO pageReqDTO) {
        PageResult<DictDataPO> pageResult = dictDataService.getDictDataPage(pageReqDTO);
        return success(new PageResult<>(
                DictDataMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()
        ));
    }

    @GetMapping(value = "/get")
    @Operation(summary = "Get dictionary data details")
    @Parameter(name = "id", description = "ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<DictDataRespDTO> getDictData(@RequestParam("id") Long id) {
        DictDataPO dictData = dictDataService.getDictData(id);
        return success(DictDataMapper.INSTANCE.toDTO(dictData));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export dictionary data")
    @PreAuthorize("@ss.hasPermission('system:dict:export')")
    // @ApiAccessLog(operateType = EXPORT)
    public void export(HttpServletResponse response, @Valid DictDataPageReqDTO exportReqDTO) throws IOException {
        exportReqDTO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DictDataPO> list = dictDataService.getDictDataPage(exportReqDTO).getList();

        // Output
        ExcelUtils.write(response, "Dictionary Data.xlsx", "Data", DictDataRespDTO.class,
                DictDataMapper.INSTANCE.toDTOList(list));
    }

}
