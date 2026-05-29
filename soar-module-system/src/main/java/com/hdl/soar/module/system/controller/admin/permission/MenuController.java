package com.hdl.soar.module.system.controller.admin.permission;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuListReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuRespDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import com.hdl.soar.module.system.mapper.permission.MenuMapper;
import com.hdl.soar.module.system.service.permission.MenuService;
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

import java.util.Comparator;
import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Menu")
@Validated
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuController {

    MenuService menuService;

    @PostMapping("/create")
    @Operation(summary = "Create menu")
    @PreAuthorize("@ss.hasPermission('system:menu:create')")
    public CommonResult<Long> createMenu(@Valid @RequestBody MenuSaveReqDTO createReqDTO) {
        Long menuId = menuService.createMenu(createReqDTO);
        return success(menuId);
    }

    @PutMapping("/update")
    @Operation(summary = "Update Menu")
    @PreAuthorize("@ss.hasPermission('system:menu:update')")
    public CommonResult<Boolean> updateMenu(@Valid @RequestBody MenuSaveReqDTO updateReqDTO) {
        menuService.updateMenu(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete menu")
    @Parameter(name = "id", description = "Menu ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:menu:delete')")
    public CommonResult<Boolean> deleteMenu(@RequestParam("id") Long id) {
        menuService.deleteMenu(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "Batch delete menus")
    @Parameter(name = "ids", description = "List of IDs", required = true)
    @PreAuthorize("@ss.hasPermission('system:menu:delete')")
    public CommonResult<Boolean> deleteMenuList(@RequestParam("ids") List<Long> ids) {
        menuService.deleteMenuList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get menu information")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public CommonResult<MenuRespDTO> getMenu(Long id) {
        MenuPO menu = menuService.getMenu(id);
        return success(MenuMapper.INSTANCE.toDTO(menu));
    }

    @GetMapping("/list")
    @Operation(summary = "Get menu list", description = "Used for the [Menu Management] page")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public CommonResult<List<MenuRespDTO>> getMenuList(MenuListReqDTO reqDTO) {
        List<MenuPO> list = menuService.getMenuList(reqDTO);
        list.sort(Comparator.comparing(MenuPO::getSort));
        return success(MenuMapper.INSTANCE.toDTO(list));
    }

    @GetMapping({"/list-all-simple", "simple-list"})
    @Operation(
            summary = "Get simplified menu list",
            description = "Only includes enabled menus, used for the [Assign Menu to Role] feature. In a multi-tenant scenario, only menus included in the tenant's package will be returned"
    )
    public CommonResult<List<MenuSimpleRespDTO>> getSimpleMenuList() {
        List<MenuPO> list = menuService.getMenuListByTenant();
        list = menuService.filterDisableMenus(list);
        list.sort(Comparator.comparing(MenuPO::getSort));
        return success(MenuMapper.INSTANCE.toSimpleDTO(list));
    }

}
