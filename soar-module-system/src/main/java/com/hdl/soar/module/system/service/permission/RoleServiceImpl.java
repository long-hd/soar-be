package com.hdl.soar.module.system.service.permission;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.util.collection.CollectionUtils;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import com.hdl.soar.module.system.dal.entity.permission.UserRolePO;
import com.hdl.soar.module.system.dal.postgres.permission.RoleRepository;
import com.hdl.soar.module.system.dal.postgres.permission.UserRoleRepository;
import com.hdl.soar.module.system.dal.redis.RedisKeyConstants;
import com.hdl.soar.module.system.enums.permission.RoleCodeEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;
    UserRoleRepository userRoleRepository;

    @Override
    public Set<Long> getUserIdsByRoleIds(Collection<Long> roleIds) {
        return convertSet(userRoleRepository.findAllByRoleIdIn(roleIds), UserRolePO::getUserId);
    }

    @Override
    public Set<Long> getRoleIdsByUserId(Long userId) {
        return userRoleRepository.findAllByUserId(userId);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.USER_ROLE_ID_LIST, key = "#userId")
    public Set<Long> getRoleIdsByUserIdFromCache(Long userId) {
        return getRoleIdsByUserId(userId);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.ROLE, key = "#id", unless = "#result == null")
    public RolePO getRoleFromCache(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public List<RolePO> getRolesFromCache(Collection<Long> roleIds) {
        if(CollectionUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        // Use a for-loop here to retrieve data from the cache,
        // mainly because Spring CacheManager does not support batch operations.
        RoleServiceImpl self = getSelf();
        return CollectionUtils.convertList(roleIds, self::getRoleFromCache);
    }


    @Override
    public List<RolePO> getEnableRolesByUserIdFromCache(Long userId) {
        // Get role IDs owned by the user
        Set<Long> roleIds = getSelf().getRoleIdsByUserIdFromCache(userId);

        // Fetch role objects and filter out disabled ones
        List<RolePO> roles = getSelf().getRolesFromCache(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus()));

        return roles;
    }

    @Override
    public boolean hasAnySuperAdmin(Collection<Long> roleIds) {
        if (CollectionUtil.isEmpty(roleIds)) {
            return false;
        }
        RoleServiceImpl self = getSelf();
        return roleIds.stream().anyMatch(roleId -> {
            RolePO role = self.getRoleFromCache(roleId);
            return role != null && RoleCodeEnum.isSuperAdmin(role.getCode());
        });
    }

    private RoleServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
