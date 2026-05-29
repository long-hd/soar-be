package com.hdl.soar.module.system.service.permission;

import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuListReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * Menu Service interface
 */
public interface MenuService {

    /**
     * Create a menu
     *
     * @param createReqDTO menu information
     * @return the created menu ID
     */
    Long createMenu(@Valid MenuSaveReqDTO createReqDTO);

    /**
     * Update menu
     *
     * @param updateReqDTO menu information
     */
    void updateMenu(@Valid MenuSaveReqDTO updateReqDTO);

    /**
     * Delete menu
     *
     * @param id menu ID
     */
    void deleteMenu(Long id);

    /**
     * Batch delete menus
     *
     * @param ids array of menu IDs
     */
    void deleteMenuList(List<Long> ids);

    /**
     * Get menu
     *
     * @param id menu ID
     * @return menu
     */
    MenuPO getMenu(Long id);

    /**
     * Get all menu list
     *
     * @return menu list
     */
    List<MenuPO> getMenuList();

    /**
     * Get menu list
     *
     * @param menuIds menu ID collection
     * @return menu list
     */
    List<MenuPO> getMenuList(Collection<Long> menuIds);

    /**
     * Filter menu list
     *
     * @param reqDTO filter condition request VO
     * @return menu list
     */
    List<MenuPO> getMenuList(MenuListReqDTO reqDTO);

    /**
     * Filter menu list based on tenant
     * Note: if it is a system tenant, all menus will still be returned
     *
     * @return menu list
     */
    List<MenuPO> getMenuListByTenant();

    /**
     * Filter out disabled menus and their child menus
     *
     * @param menuList menu list
     * @return filtered menu list
     */
    List<MenuPO> filterDisableMenus(List<MenuPO> menuList);

    /**
     * Get the list of menu IDs associated with the given permission.
     *
     * @param permission permission identifier
     * @return list of menu IDs
     */
    List<Long> getMenuIdsByPermissionFromCache(String permission);


}
