package com.hdl.soar.module.system.service.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.annotations.VisibleForTesting;
import com.hdl.soar.framework.common.biz.infra.config.ConfigCommonApi;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.exception.ServiceException;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.framework.operatelog.core.annotation.OperateLog;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.module.system.controller.admin.user.dto.profile.UserProfileUpdatePasswordReqDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.profile.UserProfileUpdateReqDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserImportExcelDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserImportRespDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserPageReqDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.user.UserSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO;
import com.hdl.soar.module.system.dal.entity.dept.PostPO;
import com.hdl.soar.module.system.dal.entity.dept.UserPostPO;
import com.hdl.soar.module.system.dal.entity.tenant.TenantPO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO_;
import com.hdl.soar.module.system.dal.postgres.dept.UserPostRepository;
import com.hdl.soar.module.system.dal.postgres.permission.UserRoleRepository;
import com.hdl.soar.module.system.dal.postgres.user.AdminUserRepository;
import com.hdl.soar.module.system.enums.common.SexEnum;
import com.hdl.soar.module.system.mapper.user.AdminUserMapper;
import com.hdl.soar.module.system.service.dept.DeptService;
import com.hdl.soar.module.system.service.dept.PostService;
import com.hdl.soar.module.system.service.tenant.TenantService;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;
import static com.hdl.soar.module.system.enums.OperateLogConstants.*;

/**
 * Backend User Service Implementation Class
 */
@Slf4j
@Service("adminUserService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserServiceImpl implements AdminUserService {

    static final String USER_INIT_PASSWORD_KEY = "system.user.init-password";
    static final String USER_REGISTER_ENABLED_KEY = "system.user.register-enabled";

    ConfigCommonApi configApi;

    AdminUserRepository adminUserRepository;
    UserPostRepository userPostRepository;
    UserRoleRepository userRoleRepository;

    DeptService deptService;
    PostService postService;
    TenantService tenantService;

    PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperateLog(module = USER_MODULE, name = "Create User",
            bizId = "#result", content = USER_CREATE_CONTENT)
    public Long createUser(UserSaveReqDTO createReqDTO) {
        // 0. validate tenant account limit
        validateTenantAccountLimit();
        // 1. Validate
        validateUserForCreateOrUpdate(null, createReqDTO);

        // 2. Create user
        AdminUserPO user = AdminUserMapper.INSTANCE.toPO(createReqDTO);
        user.setStatus(CommonStatusEnum.ENABLE); // Default enabled
        user.setPassword(encodePassword(createReqDTO.getPassword()));
        adminUserRepository.save(user);

        // 3. Sync user-post join table
        createUserPosts(user.getId(), createReqDTO.getPostIds());

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperateLog(module = USER_MODULE, name = "Update User",
            bizId = "#reqDTO.id", content = USER_UPDATE_CONTENT)
    public void updateUser(UserSaveReqDTO updateReqDTO) {
        // 1. Validate exists
        AdminUserPO existing = adminUserRepository.findById(updateReqDTO.getId())
                .orElseThrow(() -> exception(USER_NOT_EXISTS));

        // 2. Validate uniqueness + associations
        validateUserForCreateOrUpdate(updateReqDTO.getId(), updateReqDTO);

        // 3. Update user (password NOT updated here)
        AdminUserMapper.INSTANCE.updatePO(updateReqDTO, existing);
        adminUserRepository.save(existing);

        // 4. Sync user-post join table (diff-based)
        updateUserPosts(updateReqDTO.getId(), updateReqDTO.getPostIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperateLog(module = USER_MODULE, name = "Delete User",
            bizId = "#id", content = USER_DELETE_CONTENT)
    public void deleteUser(Long id) {
        // 1. Validate exists
        adminUserRepository.findById(id)
                .orElseThrow(() -> exception(USER_NOT_EXISTS));
        // 2. Delete user
        adminUserRepository.deleteById(id);
        // 3. Cleanup join tables
        userRoleRepository.deleteByUserId(id);
        userPostRepository.deleteByUserId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserList(List<Long> ids) {
        // 1. Validate all exist
        List<AdminUserPO> users = adminUserRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            throw exception(USER_NOT_EXISTS);
        }
        // 2. Delete users
        adminUserRepository.deleteAllById(ids);
        // 3. Cleanup join tables
        ids.forEach(id -> {
            userRoleRepository.deleteByUserId(id);
            userPostRepository.deleteByUserId(id);
        });
    }

    @Override
    @OperateLog(module = USER_MODULE, name = "Reset Password",
            bizId = "#id", content = USER_RESET_PWD_CONTENT)
    public void updateUserPassword(Long id, String password) {
        AdminUserPO user = adminUserRepository.findById(id)
                .orElseThrow(() -> exception(USER_NOT_EXISTS));
        user.setPassword(encodePassword(password));
        adminUserRepository.save(user);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        AdminUserPO user = adminUserRepository.findById(id)
                .orElseThrow(() -> exception(USER_NOT_EXISTS));
        user.setStatus(CommonStatusEnum.of(status));
        adminUserRepository.save(user);
        // TODO: If disabled, revoke OAuth2 tokens (implement when OAuth2TokenService is ready)
    }

    @Override
    public PageResult<AdminUserPO> getUserPage(UserPageReqDTO pageReqDTO) {
        // 1. Build dept IDs (includes children)
        Set<Long> deptIds = getDeptCondition(pageReqDTO.getDeptId());

        // 2. Build specification
        Specification<AdminUserPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, AdminUserPO_.username, pageReqDTO.getUsername());
            likeIfPresent(predicates, cb, root, AdminUserPO_.mobile, pageReqDTO.getMobile());
            eqIfPresent(predicates, cb, root, AdminUserPO_.status, CommonStatusEnum.of(pageReqDTO.getStatus()));
            betweenIfPresent(predicates, cb, root, AdminUserPO_.createTime, pageReqDTO.getCreateTime());
            // Dept filter: IN (deptId + child dept IDs)
            if (CollUtil.isNotEmpty(deptIds)) {
                predicates.add(root.get(AdminUserPO_.deptId).in(deptIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Order.asc(AdminUserPO_.ID)));
        Page<AdminUserPO> page = adminUserRepository.findAll(spec, pageable);
        return PageUtils.toPageResult(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserImportRespDTO importUserList(List<UserImportExcelDTO> importUsers, boolean updateSupport) {
        // 1. Validate
        if (CollUtil.isEmpty(importUsers)) {
            throw exception(USER_IMPORT_LIST_IS_EMPTY);
        }

        String initPassword = configApi.getConfigValueByKey(USER_INIT_PASSWORD_KEY);
        if (StrUtil.isBlank(initPassword)) {
            throw exception(USER_IMPORT_INIT_PASSWORD);
        }

        // 2. Process each row
        List<String> createUsernames = new ArrayList<>();
        List<String> updateUsernames = new ArrayList<>();
        Map<String, String> failureUsernames = new LinkedHashMap<>();

        AtomicInteger index = new AtomicInteger(1);
        importUsers.forEach(importUser -> {
            int currentIndex = index.getAndIncrement();

            // 1. Validate business rules
            try {
                validateUserForImport(importUser);
            } catch (ServiceException ex) {
                String key = StrUtil.blankToDefault(importUser.getUsername(), "Row " + currentIndex);
                failureUsernames.put(key, ex.getMessage());
                return;
            }

            // 2. Check if user exists
            Optional<AdminUserPO> existingOpt = adminUserRepository.findByUsername(importUser.getUsername());

            if (existingOpt.isEmpty()) {
                // Create new user
                AdminUserPO user = new AdminUserPO();
                user.setUsername(importUser.getUsername());
                user.setNickname(importUser.getNickname());
                user.setDeptId(importUser.getDeptId());
                user.setEmail(importUser.getEmail());
                user.setMobile(importUser.getMobile());
                user.setSex(SexEnum.of(importUser.getSex()));
                user.setStatus(importUser.getStatus() != null
                        ? CommonStatusEnum.of(importUser.getStatus())
                        : CommonStatusEnum.ENABLE);
                user.setPassword(encodePassword(initPassword));
                adminUserRepository.save(user);
                createUsernames.add(importUser.getUsername());
                return;
            }

            // User exists
            if (!updateSupport) {
                failureUsernames.put(importUser.getUsername(), USER_USERNAME_EXISTS.getMsg());
                return;
            }
            // Update existing user
            AdminUserPO existing = existingOpt.get();
            existing.setNickname(importUser.getNickname());
            existing.setDeptId(importUser.getDeptId());
            existing.setEmail(importUser.getEmail());
            existing.setMobile(importUser.getMobile());
            existing.setSex(SexEnum.of(importUser.getSex()));
            if (importUser.getStatus() != null) {
                existing.setStatus(CommonStatusEnum.of(importUser.getStatus()));
            }
            adminUserRepository.save(existing);
            updateUsernames.add(importUser.getUsername());
        });

        return UserImportRespDTO.builder()
                .createUsernames(createUsernames)
                .updateUsernames(updateUsernames)
                .failureUsernames(failureUsernames)
                .build();
    }

    @Override
    public List<AdminUserPO> getUserListByStatus(CommonStatusEnum status) {
        return adminUserRepository.findAllByStatus(status);
    }

    @Override
    public void updateUserLoginIp(Long id, String loginIp) {
        adminUserRepository.findById(id).ifPresent(adminUser -> {
            adminUser.setLoginIp(loginIp);
            adminUser.setLoginDate(Instant.now());
            adminUserRepository.save(adminUser);
        });
    }

    @Override
    public AdminUserPO getUserByUsername(String username) {
        return adminUserRepository.findByUsername(username).orElse(null);
    }

    @Override
    public AdminUserPO getUser(Long id) {
        return adminUserRepository.findById(id).orElse(null);
    }

    @Override
    public boolean isPasswordMatch(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public void updateUserProfile(Long userId, UserProfileUpdateReqDTO reqDTO) {
        // 1. Validate exists
        AdminUserPO user = adminUserRepository.findById(userId)
                .orElseThrow(() -> exception(USER_NOT_EXISTS));

        // 2. Validate uniqueness (only for changed fields)
        validateMobileUnique(userId, reqDTO.getMobile());
        validateEmailUnique(userId, reqDTO.getEmail());

        // 3. MapStruct partial update (null fields skipped)
        AdminUserMapper.INSTANCE.updateProfilePO(reqDTO, user);
        adminUserRepository.save(user);
    }

    @Override
    public void updateUserProfilePassword(Long userId, UserProfileUpdatePasswordReqDTO reqDTO) {
        // 1. Validate exists
        AdminUserPO user = adminUserRepository.findById(userId)
                .orElseThrow(() -> exception(USER_NOT_EXISTS));

        // 2. Verify old password
        if (!isPasswordMatch(reqDTO.getOldPassword(), user.getPassword())) {
            throw exception(USER_PASSWORD_FAILED);
        }

        // 3. Update to new password
        user.setPassword(encodePassword(reqDTO.getNewPassword()));
        adminUserRepository.save(user);
    }

    // =========== Utilities method

    /**
     * Get dept ID + all child dept IDs for filtering.
     *
     * @param deptId department ID
     */
    private Set<Long> getDeptCondition(Long deptId) {
        if (deptId == null) {
            return Collections.emptySet();
        }
        Set<Long> deptIds = deptService.getChildDeptList(deptId).stream()
                .map(DeptPO::getId)
                .collect(Collectors.toSet());
        deptIds.add(deptId); // Include self
        return deptIds;
    }

    // ========== Post sync ==========

    private void createUserPosts(Long userId, Set<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return;
        }
        List<UserPostPO> userPosts = postIds.stream()
                .map(postId -> UserPostPO.builder()
                        .userId(userId)
                        .postId(postId)
                        .build())
                .collect(Collectors.toList());
        userPostRepository.saveAll(userPosts);
    }

    private void updateUserPosts(Long userId, Set<Long> postIds) {
        Set<Long> newPostIds = CollUtil.emptyIfNull(postIds);
        // Get existing post IDs from join table
        Set<Long> dbPostIds = userPostRepository.findAllByUserId(userId).stream()
                .map(UserPostPO::getPostId)
                .collect(Collectors.toSet());

        // Calculate diff
        Collection<Long> createPostIds = CollUtil.subtract(newPostIds, dbPostIds);
        Collection<Long> deletePostIds = CollUtil.subtract(dbPostIds, newPostIds);

        // Apply diff
        if (CollUtil.isNotEmpty(createPostIds)) {
            List<UserPostPO> newEntries = createPostIds.stream()
                    .map(postId -> UserPostPO.builder()
                            .userId(userId)
                            .postId(postId)
                            .build())
                    .collect(Collectors.toList());
            userPostRepository.saveAll(newEntries);
        }
        if (CollUtil.isNotEmpty(deletePostIds)) {
            userPostRepository.deleteByUserIdAndPostIdIn(userId, deletePostIds);
        }
    }

    // ========== Validation ==========

    private void validateTenantAccountLimit() {
        // Tenant
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        TenantPO tenant = tenantService.getTenant(tenantId);

        // Validate user count
        long count = adminUserRepository.count();
        if(count >= tenant.getAccountCount()) {
            throw exception(USER_COUNT_MAX, tenant.getAccountCount());
        }
    }

    private void validateUserForCreateOrUpdate(Long id, UserSaveReqDTO reqDTO) {
        // Username unique
        validateUsernameUnique(id, reqDTO.getUsername());
        // Mobile unique
        validateMobileUnique(id, reqDTO.getMobile());
        // Email unique
        validateEmailUnique(id, reqDTO.getEmail());
        // Dept exists + enabled
        validateDeptEnabled(reqDTO.getDeptId());
        // Posts exist + enabled
        validatePostsEnabled(reqDTO.getPostIds());
    }

    private void validateUserForImport(UserImportExcelDTO importUser) {
        // Mobile unique
        validateMobileUnique(null, importUser.getMobile());
        // Email unique
        validateEmailUnique(null, importUser.getEmail());
        // Dept exists + enabled
        validateDeptEnabled(importUser.getDeptId());
    }

    @VisibleForTesting
    void validateUsernameUnique(Long id, String username) {
        if (StrUtil.isBlank(username)) {
            return;
        }
        Optional<AdminUserPO> existing = adminUserRepository.findByUsername(username);
        if (existing.isEmpty()) {
            return;
        }
        if (id == null || !existing.get().getId().equals(id)) {
            throw exception(USER_USERNAME_EXISTS);
        }
    }

    @VisibleForTesting
    void validateMobileUnique(Long id, String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return;
        }
        Optional<AdminUserPO> existing = adminUserRepository.findByMobile(mobile);
        if (existing.isEmpty()) {
            return;
        }
        if (id == null || !existing.get().getId().equals(id)) {
            throw exception(USER_MOBILE_EXISTS);
        }
    }

    @VisibleForTesting
    void validateEmailUnique(Long id, String email) {
        if (StrUtil.isBlank(email)) {
            return;
        }
        Optional<AdminUserPO> existing = adminUserRepository.findByEmail(email);
        if (existing.isEmpty()) {
            return;
        }
        if (id == null || !existing.get().getId().equals(id)) {
            throw exception(USER_EMAIL_EXISTS);
        }
    }

    private void validateDeptEnabled(Long deptId) {
        if (deptId == null) {
            return;
        }
        DeptPO dept = deptService.getDept(deptId); // Throws DEPT_NOT_FOUND if not exists
        if (dept.getStatus() != CommonStatusEnum.ENABLE) {
            throw exception(DEPT_NOT_ENABLE, dept.getName());
        }
    }

    private void validatePostsEnabled(Set<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return;
        }
        for (Long postId : postIds) {
            PostPO post = postService.getPost(postId); // Throws POST_NOT_FOUND if not exists
            if (post.getStatus() != CommonStatusEnum.ENABLE) {
                throw exception(POST_NOT_ENABLE, post.getName());
            }
        }
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

}
