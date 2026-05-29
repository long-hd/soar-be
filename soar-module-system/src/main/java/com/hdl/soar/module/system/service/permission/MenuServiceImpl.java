package com.hdl.soar.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.annotations.VisibleForTesting;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuListReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.menu.MenuSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import com.hdl.soar.module.system.dal.entity.permission.MenuPO_;
import com.hdl.soar.module.system.dal.postgres.permission.MenuRepository;
import com.hdl.soar.module.system.dal.postgres.permission.RoleMenuRepository;
import com.hdl.soar.module.system.dal.redis.RedisKeyConstants;
import com.hdl.soar.module.system.enums.permission.MenuTypeEnum;
import com.hdl.soar.module.system.mapper.permission.MenuMapper;
import com.hdl.soar.module.system.service.tenant.TenantService;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.convertList;
import static com.hdl.soar.framework.common.util.collection.CollectionUtils.convertMap;
import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;

/**
 * Menu Service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuServiceImpl implements MenuService {

    MenuRepository menuRepository;
    RoleMenuRepository roleMenuRepository;

    TenantService tenantService;

    @Override
    @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST,
            key = "#createReqDTO.permission",
            condition = "#createReqDTO.permission != null")
    public Long createMenu(MenuSaveReqDTO createReqDTO) {
        // 1. Validate
        validateParentMenu(createReqDTO.getParentId(), null);
        validateMenuName(createReqDTO.getParentId(), createReqDTO.getName(), null);
        validateMenuComponentName(createReqDTO.getComponentName(), null);

        // 2. Save
        MenuPO menu = MenuMapper.INSTANCE.toPO(createReqDTO);
        initMenuProperty(menu);
        menuRepository.save(menu);
        return menu.getId();
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    public void updateMenu(MenuSaveReqDTO updateReqDTO) {
        // 1. Validate exists
        MenuPO existing = menuRepository.findById(updateReqDTO.getId())
                .orElseThrow(() -> exception(MENU_NOT_EXISTS));

        // 2. Validate parent + uniqueness
        validateParentMenu(updateReqDTO.getParentId(), updateReqDTO.getId());
        validateMenuName(updateReqDTO.getParentId(), updateReqDTO.getName(), updateReqDTO.getId());
        validateMenuComponentName(updateReqDTO.getComponentName(), updateReqDTO.getId());

        // 3. Update
        MenuMapper.INSTANCE.updatePO(updateReqDTO, existing);
        initMenuProperty(existing);
        menuRepository.save(existing);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    // allEntries clears all cache entries because we don't know the permission for each id; clearing all is simple and effective
    public void deleteMenu(Long id) {
        // 1. Validate exists
        menuRepository.findById(id)
                .orElseThrow(() -> exception(MENU_NOT_EXISTS));
        // 2. Validate no children
        if (menuRepository.countByParentId(id) > 0) {
            throw exception(MENU_EXISTS_CHILDREN);
        }
        // 3. Soft delete menu
        menuRepository.deleteById(id);
        // 4. Cleanup role-menu associations (same domain, direct repository access)
        roleMenuRepository.deleteByMenuId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    // Spring Cache does not support batch eviction by ids, so we clear everything
    public void deleteMenuList(List<Long> ids) {
        // 1. Validate all exist
        List<MenuPO> menus = menuRepository.findAllById(ids);
        if (menus.size() != ids.size()) {
            throw exception(MENU_NOT_EXISTS);
        }
        // 2. Validate none have children
        for (Long id : ids) {
            if (menuRepository.countByParentId(id) > 0) {
                throw exception(MENU_EXISTS_CHILDREN);
            }
        }
        // 3. Soft delete menus
        menuRepository.deleteAllById(ids);
        // 4. Cleanup role-menu associations
        ids.forEach(roleMenuRepository::deleteByMenuId);
    }

    @Override
    public MenuPO getMenu(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> exception(MENU_NOT_EXISTS));
    }

    @Override
    public List<MenuPO> getMenuList() {
        return menuRepository.findAll();
    }

    @Override
    public List<MenuPO> getMenuList(Collection<Long> menuIds) {
        return menuRepository.findAllByIdIn(menuIds);
    }

    @Override
    public List<MenuPO> getMenuList(MenuListReqDTO reqDTO) {
        Specification<MenuPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, MenuPO_.name, reqDTO.getName());
            eqIfPresent(predicates, cb, root, MenuPO_.status, CommonStatusEnum.of(reqDTO.getStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return menuRepository.findAll(spec);
    }

    @Override
    public List<MenuPO> getMenuListByTenant() {
        List<MenuPO> menus = menuRepository.findAll();

        // Filter by tenant package
        Set<Long> tenantMenuIds = tenantService.getTenantMenuIds();

        if (tenantMenuIds != null) {
            menus.removeIf(menu -> !tenantMenuIds.contains(menu.getId()));
        }
        return menus;
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

    /**
     * Validate whether the parent menu is valid
     *
     * <p>
     * 1. Cannot set itself as its own parent menu
     * 2. Parent menu does not exist
     * 3. Parent menu must be of type {@link MenuTypeEnum#MENU}
     *
     * @param parentId parent menu ID
     * @param childId  current menu ID
     */
    @VisibleForTesting
    void validateParentMenu(Long parentId, Long childId) {
        if (parentId == null || MenuPO.ID_ROOT.equals(parentId)) {
            return;
        }
        // Cannot set self as parent
        if (parentId.equals(childId)) {
            throw exception(MENU_PARENT_ERROR);
        }
        // Parent must exist
        MenuPO parent = menuRepository.findById(parentId)
                .orElseThrow(() -> exception(MENU_PARENT_NOT_EXISTS));
        // Parent must be DIR or MENU (not BUTTON)
        if (!MenuTypeEnum.DIR.equals(parent.getType())
                && !MenuTypeEnum.MENU.equals(parent.getType())) {
            throw exception(MENU_PARENT_NOT_DIR_OR_MENU);
        }
    }

    /**
     * Validate whether the menu is valid
     *
     * <p>
     * 1. Check whether a menu with the same name already exists under the same parent menu
     *
     * @param name     menu name
     * @param parentId parent menu ID
     * @param id       menu ID
     */
    @VisibleForTesting
    void validateMenuName(Long parentId, String name, Long id) {
        Optional<MenuPO> existing = menuRepository.findByParentIdAndName(parentId, name);
        if (existing.isEmpty()) {
            return;
        }
        // If id is null, it means there is no need to compare menus with the same id
        if (id == null || !existing.get().getId().equals(id)) {
            throw exception(MENU_NAME_DUPLICATE);
        }
    }

    /**
     * Validate whether the menu component name is valid
     *
     * @param componentName component name
     * @param id            menu ID
     */
    @VisibleForTesting
    void validateMenuComponentName(String componentName, Long id) {
        if (StrUtil.isBlank(componentName)) {
            return;
        }
        Optional<MenuPO> existing = menuRepository.findByComponentName(componentName);
        if (existing.isEmpty()) {
            return;
        }
        // If the id is null, it means there is no need to compare whether it is the same menu by id
        if (id == null || !existing.get().getId().equals(id)) {
            throw exception(MENU_COMPONENT_NAME_DUPLICATE);
        }
    }

    /**
     * Initialize common attributes of the menu.
     *
     * <p>
     * For example, only directory or menu type items will have an icon set
     *
     * @param menu menu object
     */
    private void initMenuProperty(MenuPO menu) {
        // Button type doesn't need component/icon/path — clear them.
        if (MenuTypeEnum.BUTTON.equals(menu.getType())) {
            menu.setComponent("");
            menu.setComponentName("");
            menu.setIcon("");
            menu.setPath("");
        }
    }

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
