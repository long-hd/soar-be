package com.hdl.soar.module.system.controller.admin.dict;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeSaveReqDTO;
import com.hdl.soar.module.system.service.dict.DictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

}
