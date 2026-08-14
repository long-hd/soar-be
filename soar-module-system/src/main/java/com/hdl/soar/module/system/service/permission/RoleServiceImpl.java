package com.hdl.soar.module.system.service.permission;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.collection.CollectionUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RolePageReqDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RoleSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import com.hdl.soar.module.system.dal.entity.permission.UserRolePO;
import com.hdl.soar.module.system.dal.postgres.permission.RoleMenuRepository;
import com.hdl.soar.module.system.dal.postgres.permission.RoleRepository;
import com.hdl.soar.module.system.dal.postgres.permission.UserRoleRepository;
import com.hdl.soar.module.system.dal.redis.RedisKeyConstants;
import com.hdl.soar.module.system.enums.permission.DataScopeEnum;
import com.hdl.soar.module.system.enums.permission.RoleCodeEnum;
import com.hdl.soar.module.system.enums.permission.RoleTypeEnum;
import com.hdl.soar.module.system.mapper.permission.RoleMapper;
import com.hdl.soar.module.system.dal.entity.permission.RolePO_;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.*;
import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;
    UserRoleRepository userRoleRepository;
    RoleMenuRepository roleMenuRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    // @LogRecord(type = SYSTEM_ROLE_TYPE, subType = SYSTEM_ROLE_CREATE_SUB_TYPE, bizNo = "{{#role.id}}", success = SYSTEM_ROLE_CREATE_SUCCESS)
    public Long createRole(RoleSaveReqDTO createReqDTO, RoleTypeEnum type) {
        // 1. Validate
        validateRoleDuplicate(createReqDTO.getName(), createReqDTO.getCode(), null);

        // 2. Save with defaults
        RolePO role = RoleMapper.INSTANCE.toPO(createReqDTO);
        role.setStatus(CommonStatusEnum.ENABLE);
        role.setType(RoleTypeEnum.CUSTOM);
        role.setDataScope(DataScopeEnum.ALL);
        roleRepository.save(role);

        return role.getId();
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.ROLE, key = "#updateReqDTO.id")
    // @LogRecord(type = SYSTEM_ROLE_TYPE, subType = SYSTEM_ROLE_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}", success = SYSTEM_ROLE_UPDATE_SUCCESS)
    public void updateRole(RoleSaveReqDTO updateReqDTO) {
        // 1. Validate exists + not system role
        RolePO existing = validateRoleForUpdate(updateReqDTO.getId());
        // 2. Validate unique
        validateRoleDuplicate(updateReqDTO.getName(), updateReqDTO.getCode(), updateReqDTO.getId());

        // 3. Update (only fields in DTO — type/dataScope untouched)
        RoleMapper.INSTANCE.updatePO(updateReqDTO, existing);
        roleRepository.save(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = RedisKeyConstants.ROLE, key = "#id"),
            @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST, allEntries = true),
            @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, allEntries = true)
    })
    public void deleteRole(Long id) {
        // 1. Validate exists + not system role
        validateRoleForUpdate(id);
        // 2. Delete role
        roleRepository.deleteById(id);
        // 3. Cleanup join tables
        roleMenuRepository.deleteByRoleId(id);
        userRoleRepository.deleteByRoleId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = RedisKeyConstants.ROLE, allEntries = true),
            @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST, allEntries = true),
            @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, allEntries = true)
    })
    public void deleteRoleList(List<Long> ids) {
        // 1. Validate all exist + none are system roles
        ids.forEach(this::validateRoleForUpdate);
        // 2. Delete roles
        roleRepository.deleteAllById(ids);
        // 3. Cleanup join tables
        ids.forEach(id -> {
            roleMenuRepository.deleteByRoleId(id);
            userRoleRepository.deleteByRoleId(id);
        });
    }

    @Override
    public RolePO getRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> exception(ROLE_NOT_EXISTS));
    }

    @Override
    public PageResult<RolePO> getRolePage(RolePageReqDTO pageReqDTO) {
        Specification<RolePO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, RolePO_.name, pageReqDTO.getName());
            likeIfPresent(predicates, cb, root, RolePO_.code, pageReqDTO.getCode());
            eqIfPresent(predicates, cb, root, RolePO_.status, CommonStatusEnum.of(pageReqDTO.getStatus()));
            betweenIfPresent(predicates, cb, root, RolePO_.createTime, pageReqDTO.getCreateTime());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Sort sort = Sort.by(Sort.Order.asc(RolePO_.SORT));
        Pageable pageable = PageUtils.toPageable(pageReqDTO, sort);

        Page<RolePO> page = roleRepository.findAll(spec, pageable);
        return PageUtils.toPageResult(page);
    }

    @Override
    public List<RolePO> getRoleListByStatus(CommonStatusEnum status) {
        return roleRepository.findAllByStatus(status);
    }

    @Override
    public Set<Long> getUserIdsByRoleIds(Collection<Long> roleIds) {
        return convertSet(userRoleRepository.findAllByRoleIdIn(roleIds), UserRolePO::getUserId);
    }

    @Override
    public Set<Long> getRoleIdsByUserId(Long userId) {
        return userRoleRepository.findRoleIdsByUserId(userId);
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
    public List<RolePO> getRolesByIdIn(Collection<Long> roleIds) {
        return roleRepository.findAllByIdIn(roleIds);
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
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.equals(role.getStatus()));

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

    @Override
    @CacheEvict(value = RedisKeyConstants.ROLE, key = "#roleId")
    public void updateRoleDataScope(Long roleId, Integer dataScope, Set<Long> dataScopeDeptIds) {
        RolePO role = roleRepository.findById(roleId)
                .orElseThrow(() -> exception(ROLE_NOT_EXISTS));

        // Built-in role, deletion is not allowed
        if (RoleTypeEnum.SYSTEM.equals(role.getType())) {
            throw exception(ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE);
        }

        DataScopeEnum dataScopeEnum = DataScopeEnum.of(dataScope);
        if(dataScopeEnum == null) {
            throw new IllegalArgumentException("Invalid data scope: " + dataScope);
        }

        role.setDataScope(dataScopeEnum);
        // Defensive: deptIds are only meaningful when scope = DEPT_CUSTOM
        role.setDataScopeDeptIds(
                dataScopeEnum == DataScopeEnum.DEPT_CUSTOM ? dataScopeDeptIds : null
        );
        roleRepository.save(role);
    }

    // =================== Helper

    private RoleServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    /**
     * Validate whether role unique fields are duplicated
     * <ul>
     *  <li>1. Check whether a role with the same name exists</li>
     *  <li>2. Check whether a role with the same code exists</li>
     * </ul>
     * @param name role name
     * @param code role code
     * @param id role ID
     */
    private void validateRoleDuplicate(String name, String code, Long id) {
        // Super admin code forbidden
        if (RoleCodeEnum.isSuperAdmin(code)) {
            throw exception(ROLE_ADMIN_CODE_ERROR, code);
        }
        // Name unique
        Optional<RolePO> byName = roleRepository.findByName(name);
        if (byName.isPresent() && !byName.get().getId().equals(id)) {
            throw exception(ROLE_NAME_DUPLICATE, name);
        }
        // Code unique
        if (StrUtil.isBlank(code)) {
            return;
        }
        Optional<RolePO> byCode = roleRepository.findByCode(code);
        if (byCode.isPresent() && !byCode.get().getId().equals(id)) {
            throw exception(ROLE_CODE_DUPLICATE, code);
        }
    }

    /**
     * Validate whether the role can be updated.
     * SYSTEM role must not be updated
     *
     * @param id role ID
     */
    private RolePO validateRoleForUpdate(Long id) {
        RolePO role = roleRepository.findById(id)
                .orElseThrow(() -> exception(ROLE_NOT_EXISTS));
        // System built-in role cannot be modified
        if (role.getType() == RoleTypeEnum.SYSTEM) {
            throw exception(ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE);
        }
        return role;
    }

}
