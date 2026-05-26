package com.hdl.soar.module.system.mapper.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.hdl.soar.framework.common.util.object.BeanUtils;
import com.hdl.soar.module.system.controller.admin.auth.dto.AuthPermissionInfoRespDTO;
import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.enums.permission.MenuTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.convertSet;
import static com.hdl.soar.framework.common.util.collection.CollectionUtils.filterList;
import static com.hdl.soar.module.system.dal.entity.permission.MenuPO.ID_ROOT;

@Mapper
public interface AuthMapper {
    AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    default AuthPermissionInfoRespDTO convert(AdminUserPO user, List<RolePO> roleList, List<MenuPO> menuList) {
        return AuthPermissionInfoRespDTO.builder()
                .user(BeanUtils.toBean(user, AuthPermissionInfoRespDTO.UserDTO.class))
                .roles(convertSet(roleList, RolePO::getCode))
                // Permission identifier information
                .permissions(convertSet(menuList, MenuPO::getPermission))
                // Menu tree
                .menus(buildMenuTree(menuList))
                .build();
    }

    /**
     * Build a menu tree from the menu list
     *
     * @param menuList the menu list
     * @return the menu tree
     */
    default List<AuthPermissionInfoRespDTO.MenuDTO> buildMenuTree(List<MenuPO> menuList) {
        if (CollUtil.isEmpty(menuList)) {
            return Collections.emptyList();
        }

        // Remove buttons
        menuList.removeIf(menu -> menu.getType().equals(MenuTypeEnum.BUTTON.getType()));

        // Sort to ensure menu ordering
        menuList.sort(Comparator.comparing(MenuPO::getSort));

        // Build menu tree
        // Using LinkedHashMap to preserve ordering. Stream API could also be used, but it's less readable.
        Map<Long, AuthPermissionInfoRespDTO.MenuDTO> treeNodeMap = new LinkedHashMap<>();

        menuList.forEach(menu -> treeNodeMap.put(
                menu.getId(),
                BeanUtils.toBean(menu, AuthPermissionInfoRespDTO.MenuDTO.class)
        ));

        // Build parent-child relationships
        treeNodeMap.values().stream()
                .filter(node -> ObjUtil.notEqual(node.getParentId(), ID_ROOT))
                .forEach(childNode -> {

                    // Get parent node
                    AuthPermissionInfoRespDTO.MenuDTO parentNode = treeNodeMap.get(childNode.getParentId());

                    if (parentNode == null) {
                        LoggerFactory.getLogger(getClass()).error(
                                "[buildRouterTree][resource({}) cannot find parent resource ({})]",
                                childNode.getId(), childNode.getParentId()
                        );
                        return;
                    }

                    // Add child to parent
                    if (parentNode.getChildren() == null) {
                        parentNode.setChildren(new ArrayList<>());
                    }

                    parentNode.getChildren().add(childNode);
                });

        // Return all root nodes
        return filterList(treeNodeMap.values(), node -> ID_ROOT.equals(node.getParentId()));
    }



}
