package com.hdl.soar.module.system.service.permission;

import com.hdl.soar.module.system.dal.entity.permission.MenuPO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Menu Service interface
 */
public interface MenuService {

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
