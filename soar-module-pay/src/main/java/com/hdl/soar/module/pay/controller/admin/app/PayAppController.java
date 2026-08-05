package com.hdl.soar.module.pay.controller.admin.app;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.controller.admin.app.dto.PayAppPageReqDTO;
import com.hdl.soar.module.pay.controller.admin.app.dto.PayAppRespDTO;
import com.hdl.soar.module.pay.controller.admin.app.dto.PayAppSaveReqDTO;
import com.hdl.soar.module.pay.dal.entity.app.PayAppPO;
import com.hdl.soar.module.pay.mapper.app.PayAppMapper;
import com.hdl.soar.module.pay.service.app.PayAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Payment App")
@RestController
@RequestMapping("/pay/app")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayAppController {

    PayAppService appService;

    @PostMapping("/create")
    @Operation(summary = "Create payment app")
    @PreAuthorize("@ss.hasPermission('pay:app:create')")
    public CommonResult<Long> createApp(@Valid @RequestBody PayAppSaveReqDTO createReqDTO) {
        return success(appService.createApp(createReqDTO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update payment app")
    @PreAuthorize("@ss.hasPermission('pay:app:update')")
    public CommonResult<Boolean> updateApp(@Valid @RequestBody PayAppSaveReqDTO updateReqDTO) {
        appService.updateApp(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete payment app")
    @Parameter(name = "id", description = "App ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:app:delete')")
    public CommonResult<Boolean> deleteApp(@RequestParam("id") Long id) {
        appService.deleteApp(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get payment app")
    @Parameter(name = "id", description = "App ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<PayAppRespDTO> getApp(@RequestParam("id") Long id) {
        PayAppPO app = appService.getApp(id);
        return success(PayAppMapper.INSTANCE.toDTO(app));
    }

    @GetMapping("/page")
    @Operation(summary = "Get payment app paginated list")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<PageResult<PayAppRespDTO>> getAppPage(@Valid PayAppPageReqDTO pageReqDTO) {
        PageResult<PayAppPO> pageResult = appService.getAppPage(pageReqDTO);
        return success(new PageResult<>(
                PayAppMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()));
    }

}
