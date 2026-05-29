package com.hdl.soar.module.system.controller.admin.permission;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RolePageReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RoleRespDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RoleSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import com.hdl.soar.module.system.mapper.RoleMapper;
import com.hdl.soar.module.system.service.permission.RoleService;
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
import java.util.Comparator;
import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Management Backend - Role")
@Validated
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    RoleService roleService;

    @PostMapping("/create")
    @Operation(summary = "Create role")
    @PreAuthorize("@ss.hasPermission('system:role:create')")
    public CommonResult<Long> createRole(@Valid @RequestBody RoleSaveReqDTO createReqDTO) {
        return success(roleService.createRole(createReqDTO, null));
    }

    @PutMapping("/update")
    @Operation(summary = "Update role")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    public CommonResult<Boolean> updateRole(@Valid @RequestBody RoleSaveReqDTO updateReqDTO) {
        roleService.updateRole(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete role")
    @Parameter(name = "id", description = "Role ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:role:delete')")
    public CommonResult<Boolean> deleteRole(@RequestParam("id") Long id) {
        roleService.deleteRole(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "Batch delete roles")
    @Parameter(name = "ids", description = "ID list", required = true)
    @PreAuthorize("@ss.hasPermission('system:role:delete')")
    public CommonResult<Boolean> deleteRoleList(@RequestParam("ids") List<Long> ids) {
        roleService.deleteRoleList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get role information")
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public CommonResult<RoleRespDTO> getRole(@RequestParam("id") Long id) {
        RolePO role = roleService.getRole(id);
        return success(RoleMapper.INSTANCE.toDTO(role));
    }

    @GetMapping("/page")
    @Operation(summary = "Get role page")
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public CommonResult<PageResult<RoleRespDTO>> getRolePage(RolePageReqDTO pageReqDTO) {
        PageResult<RolePO> pageResult = roleService.getRolePage(pageReqDTO);
        return success(new PageResult<>(
                RoleMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()
        ));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "Get simplified role list",
            description = "Only enabled roles, mainly used for frontend dropdowns")
    public CommonResult<List<RoleRespDTO>> getSimpleRoleList() {
        List<RolePO> list = roleService.getRoleListByStatus(CommonStatusEnum.ENABLE);
        list.sort(Comparator.comparing(RolePO::getSort));
        return success(RoleMapper.INSTANCE.toDTOList(list));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export role Excel")
    // @ApiAccessLog(operateType = EXPORT)
    @PreAuthorize("@ss.hasPermission('system:role:export')")
    public void export(HttpServletResponse response,
                       @Validated RolePageReqDTO exportReqVO) throws IOException {

        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RolePO> list = roleService.getRolePage(exportReqVO).getList();

        // Export
        ExcelUtils.write(response,
                "role-data.xls",
                "data",
                RoleRespDTO.class,
                RoleMapper.INSTANCE.toDTOList(list));
    }

}
