package com.hdl.soar.module.system.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.system.controller.admin.user.dto.profile.UserProfileRespDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.profile.UserProfileUpdatePasswordReqDTO;
import com.hdl.soar.module.system.controller.admin.user.dto.profile.UserProfileUpdateReqDTO;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO;
import com.hdl.soar.module.system.dal.entity.dept.PostPO;
import com.hdl.soar.module.system.dal.entity.permission.RolePO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.mapper.dept.DeptMapper;
import com.hdl.soar.module.system.mapper.dept.PostMapper;
import com.hdl.soar.module.system.mapper.permission.RoleMapper;
import com.hdl.soar.module.system.mapper.user.AdminUserMapper;
import com.hdl.soar.module.system.service.dept.DeptService;
import com.hdl.soar.module.system.service.dept.PostService;
import com.hdl.soar.module.system.service.permission.RoleService;
import com.hdl.soar.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;
import static com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "Admin Backend - User Profile")
@Slf4j
@RestController
@RequestMapping("/system/user/profile")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileController {

    AdminUserService adminUserService;
    DeptService deptService;
    PostService postService;
    RoleService roleService;

    @GetMapping("/get")
    @Operation(summary = "Get current user profile")
    public CommonResult<UserProfileRespDTO> getUserProfile() {
        Long userId = getLoginUserId();
        AdminUserPO user = adminUserService.getUser(userId);

        // 1. Map user basic info
        UserProfileRespDTO respDTO = AdminUserMapper.INSTANCE.toProfileDTO(user);

        // 2. Roles
        List<RolePO> roles = roleService.getEnableRolesByUserIdFromCache(userId);
        respDTO.setRoles(RoleMapper.INSTANCE.toSimpleDTOList(roles));

        // 3. Department
        if (user.getDeptId() != null) {
            DeptPO dept = deptService.getDept(user.getDeptId());
            if (dept != null) {
                respDTO.setDept(DeptMapper.INSTANCE.toSimpleDTO(dept));
            }
        }

        // 4. Posts
        if (CollUtil.isNotEmpty(user.getPostIds())) {
            List<PostPO> posts = postService.getPostList(user.getPostIds());
            respDTO.setPosts(PostMapper.INSTANCE.toSimpleDTOList(posts));
        }

        return success(respDTO);
    }

    @PutMapping("/update")
    @Operation(summary = "Update current user profile")
    public CommonResult<Boolean> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateReqDTO reqDTO) {
        adminUserService.updateUserProfile(getLoginUserId(), reqDTO);
        return success(true);
    }

    @PutMapping("/update-password")
    @Operation(summary = "Change current user password")
    public CommonResult<Boolean> updateUserProfilePassword(
            @Valid @RequestBody UserProfileUpdatePasswordReqDTO reqDTO) {
        adminUserService.updateUserProfilePassword(getLoginUserId(), reqDTO);
        return success(true);
    }

}
