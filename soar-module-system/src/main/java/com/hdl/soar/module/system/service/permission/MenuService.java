package com.hdl.soar.module.system.service.permission;

import java.util.List;
import java.util.Set;

/**
 * Menu Service interface
 */
public interface MenuService {

    /**
     * Get the list of menu IDs associated with the given permission.
     *
     * @param permission permission identifier
     * @return list of menu IDs
     */
    List<Long> getMenuIdsByPermissionFromCache(String permission);
}
