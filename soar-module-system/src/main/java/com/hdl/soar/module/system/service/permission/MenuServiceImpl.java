package com.hdl.soar.module.system.service.permission;

import com.hdl.soar.module.system.dal.entity.permission.MenuPO;
import com.hdl.soar.module.system.dal.postgres.permission.MenuRepository;
import com.hdl.soar.module.system.dal.redis.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.*;

/**
 * Menu Service implementation
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    MenuRepository menuRepository;


    @Override
    @Cacheable(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, key = "#permission")
    public List<Long> getMenuIdsByPermissionFromCache(String permission) {
        List<MenuPO> menus = menuRepository.findAllByPermission(permission);
        return convertList(menus, MenuPO::getId);
    }

}
