package com.hdl.soar.module.system.controller.admin.tenant;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.tenant.core.aop.TenantIgnore;
import com.hdl.soar.module.system.controller.admin.tenant.dto.tenant.TenantSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.tenant.TenantPO;
import com.hdl.soar.module.system.mapper.tenant.TenantMapper;
import com.hdl.soar.module.system.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Management - Tenant")
@RestController
@RequestMapping("/system/tenant")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TenantController {

    TenantService tenantService;

    @GetMapping("/get-id-by-name")
    @PermitAll
    @TenantIgnore
    @Operation(
            summary = "Get Tenant ID by Tenant Name",
            description = "On the login page, retrieve the tenant ID based on the tenant name provided by the user"
    )
    @Parameter(
            name = "name",
            description = "Tenant Name",
            required = true,
            example = "1024"
    )
    public CommonResult<Long> getTenantIdByName(@RequestParam("name") String name) {
        TenantPO tenant = tenantService.getTenantByName(name);
        return success(tenant != null ? tenant.getId() : null);
    }

    @GetMapping("/get-by-website")
    @PermitAll
    @TenantIgnore
    @Operation(
            summary = "Get tenant information by website domain",
            description = "Retrieves tenant information based on the user's website domain on the login page"
    )
    @Parameter(
            name = "website",
            description = "Website domain",
            required = true,
            example = "www.soar.com"
    )
    public CommonResult<TenantSimpleRespDTO> getTenantByWebsite(
            @RequestParam("website")
            @Pattern(
                    regexp = "^[a-zA-Z0-9.-]+(:\\d{1,5})?$",
                    message = "Invalid website domain format"
            )
            String website) {
        TenantPO tenant = tenantService.getTenantByWebsite(website);
        if (tenant == null || CommonStatusEnum.isDisable(tenant.getStatus())) {
            return success(null);
        }
        return success(TenantMapper.INSTANCE.toSimpleDTO(tenant));
    }

}
