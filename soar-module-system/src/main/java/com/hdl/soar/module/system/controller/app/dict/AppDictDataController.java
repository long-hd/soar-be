package com.hdl.soar.module.system.controller.app.dict;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.system.controller.app.dict.dto.AppDictDataRespDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictDataPO;
import com.hdl.soar.module.system.mapper.dict.DictDataMapper;
import com.hdl.soar.module.system.mapper.dict.DictTypeMapper;
import com.hdl.soar.module.system.service.dict.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User App - Dictionary Data")
@Validated
@RestController
@RequestMapping("/system/dict-data")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppDictDataController {

    DictDataService dictDataService;

    @GetMapping("/type")
    @Operation(summary = "Get dictionary data information by dictionary type")
    @Parameter(name = "type", description = "Dictionary type", required = true, example = "common_status")
    @PermitAll
    public CommonResult<List<AppDictDataRespDTO>> getDictDataListByType(@RequestParam("type") String type) {
        List<DictDataPO> list = dictDataService.getDictDataList(CommonStatusEnum.ENABLE, type);
        return CommonResult.success(DictDataMapper.INSTANCE.toAppDTOList(list));
    }

}
