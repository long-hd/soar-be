package com.hdl.soar.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import com.hdl.soar.module.system.dal.postgres.permission.MenuRepository;
import com.hdl.soar.module.system.dal.redis.RedisKeyConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.convertList;
import static com.hdl.soar.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * Menu Service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuServiceImpl implements MenuService {

    MenuRepository menuRepository;

    @Override
    public List<MenuPO> getMenuList() {
        return menuRepository.findAll();
    }

    @Override
    public List<MenuPO> getMenuList(Collection<Long> menuIds) {
        return menuRepository.findAllByIdIn(menuIds);
    }

    @Override
    public List<MenuPO> filterDisableMenus(List<MenuPO> menuList) {
        if (CollUtil.isEmpty(menuList)){
            return Collections.emptyList();
        }
        Map<Long, MenuPO> menuMap = convertMap(menuList, MenuPO::getId);

        // Traverse the menu list, find menus that are not disabled, and add them to the enabledMenus result list
        List<MenuPO> enabledMenus = new ArrayList<>();
        Set<Long> disabledMenuCache = new HashSet<>(); // Store menus found to be disabled during recursive search to avoid duplicate searches
        for (MenuPO menu : menuList) {
            if (isMenuDisabled(menu, menuMap, disabledMenuCache)) {
                continue;
            }
            enabledMenus.add(menu);
        }
        return enabledMenus;
    }

    @Override
    @Cacheable(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, key = "#permission")
    public List<Long> getMenuIdsByPermissionFromCache(String permission) {
        List<MenuPO> menus = menuRepository.findAllByPermission(permission);
        return convertList(menus, MenuPO::getId);
    }

    // ====================== Utilities Method

    private boolean isMenuDisabled(MenuPO node, Map<Long, MenuPO> menuMap, Set<Long> disabledMenuCache) {
        // If the node has already been determined to be disabled, return immediately
        if (disabledMenuCache.contains(node.getId())) {
            return true;
        }

        // 1. First check whether the node itself is disabled
        if (CommonStatusEnum.isDisable(node.getStatus())) {
            disabledMenuCache.add(node.getId());
            return true;
        }

        // 2. If the parentId points to the root node, no further checking is needed
        Long parentId = node.getParentId();
        if (ObjUtil.equal(parentId, MenuPO.ID_ROOT)) {
            return false;
        }

        // 3. Recursively check the parent node
        MenuPO parent = menuMap.get(parentId);
        if (parent == null || isMenuDisabled(parent, menuMap, disabledMenuCache)) {
            disabledMenuCache.add(node.getId());
            return true;
        }

        return false;
    }

}
