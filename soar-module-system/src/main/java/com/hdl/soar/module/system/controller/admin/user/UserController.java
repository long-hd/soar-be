package com.hdl.soar.module.system.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.system.controller.admin.user.dto.user.*;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.enums.common.SexEnum;
import com.hdl.soar.module.system.mapper.user.AdminUserMapper;
import com.hdl.soar.module.system.service.dept.DeptService;
import com.hdl.soar.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - User")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    AdminUserService adminUserService;
    DeptService deptService;

    @PostMapping("/create")
    @Operation(summary = "Create user")
    @PreAuthorize("@ss.hasPermission('system:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody UserSaveReqDTO createReqDTO) {
        Long id = adminUserService.createUser(createReqDTO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "Update User")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody UserSaveReqDTO updateReqDTO) {
        adminUserService.updateUser(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete User")
    @Parameter(name = "id", description = "User ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        adminUserService.deleteUser(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "Batch Delete Users")
    @Parameter(name = "ids", description = "User ID list", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    public CommonResult<Boolean> deleteUserList(@RequestParam("ids") List<Long> ids) {
        adminUserService.deleteUserList(ids);
        return success(true);
    }

    @PutMapping("/update-password")
    @Operation(summary = "Reset User Password")
    @PreAuthorize("@ss.hasPermission('system:user:update-password')")
    public CommonResult<Boolean> updateUserPassword(@Valid @RequestBody UserUpdatePasswordReqDTO reqDTO) {
        adminUserService.updateUserPassword(reqDTO.getId(), reqDTO.getPassword());
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "Update User Status")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> updateUserStatus(@Valid @RequestBody UserUpdateStatusReqDTO reqDTO) {
        adminUserService.updateUserStatus(reqDTO.getId(), reqDTO.getStatus());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "Get User Page")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<PageResult<UserRespDTO>> getUserPage(@Valid UserPageReqDTO pageReqDTO) {
        // 1. Query users
        PageResult<AdminUserPO> pageResult = adminUserService.getUserPage(pageReqDTO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        // 2. Join dept names
        Set<Long> deptIds = pageResult.getList().stream()
                .map(AdminUserPO::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, DeptPO> deptMap = deptService.getDeptMap(deptIds);

        // 3. Convert to DTO with dept name
        List<UserRespDTO> dtoList = AdminUserMapper.INSTANCE.toDTOList(pageResult.getList());
        dtoList.forEach(dto -> {
            if (dto.getDeptId() != null) {
                DeptPO dept = deptMap.get(dto.getDeptId());
                if (dept != null) {
                    dto.setDeptName(dept.getName());
                }
            }
        });
        return success(new PageResult<>(dtoList, pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "Get User Detail")
    @Parameter(name = "id", description = "User ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<UserRespDTO> getUser(@RequestParam("id") Long id) {
        AdminUserPO user = adminUserService.getUser(id);
        if (user == null) {
            return success(null);
        }
        UserRespDTO dto = AdminUserMapper.INSTANCE.toDTO(user);
        if (user.getDeptId() != null) {
            DeptPO dept = deptService.getDept(user.getDeptId());
            dto.setDeptName(dept.getName());
        }
        return success(dto);
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "Get simple user list",
            description = "Only enabled users, used for dropdown")
    public CommonResult<List<UserSimpleRespDTO>> getSimpleUserList() {
        List<AdminUserPO> list = adminUserService.getUserListByStatus(CommonStatusEnum.ENABLE);
        // Join dept names
        Set<Long> deptIds = list.stream()
                .map(AdminUserPO::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, DeptPO> deptMap = deptService.getDeptMap(deptIds);

        List<UserSimpleRespDTO> dtoList = AdminUserMapper.INSTANCE.toSimpleDTOList(list);
        dtoList.forEach(dto -> {
            if (dto.getDeptId() != null) {
                DeptPO dept = deptMap.get(dto.getDeptId());
                if (dept != null) {
                    dto.setDeptName(dept.getName());
                }
            }
        });
        return success(dtoList);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export Users to Excel")
    @PreAuthorize("@ss.hasPermission('system:user:export')")
    public void exportUserExcel(HttpServletResponse response,
                                @Valid UserPageReqDTO pageReqDTO) throws IOException {
        pageReqDTO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AdminUserPO> list = adminUserService.getUserPage(pageReqDTO).getList();
        // Join dept names
        Set<Long> deptIds = list.stream()
                .map(AdminUserPO::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, DeptPO> deptMap = deptService.getDeptMap(deptIds);

        List<UserRespDTO> dtoList = AdminUserMapper.INSTANCE.toDTOList(list);
        dtoList.forEach(dto -> {
            if (dto.getDeptId() != null) {
                DeptPO dept = deptMap.get(dto.getDeptId());
                if (dept != null) {
                    dto.setDeptName(dept.getName());
                }
            }
        });

        ExcelUtils.write(response, "users.xlsx", "Users", UserRespDTO.class, dtoList);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "Download User Import Template")
    public void getImportTemplate(HttpServletResponse response) throws IOException {
        List<UserImportExcelDTO> sampleData = Arrays.asList(
                UserImportExcelDTO.builder()
                        .username("user01").nickname("User 01").deptId(1L)
                        .email("user01@example.com").mobile("0901234567")
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .sex(SexEnum.MALE.getSex()).build(),
                UserImportExcelDTO.builder()
                        .username("user02").nickname("User 02").deptId(2L)
                        .email("user02@example.com").mobile("0901234568")
                        .status(CommonStatusEnum.DISABLE.getStatus())
                        .sex(SexEnum.FEMALE.getSex()).build()
        );
        ExcelUtils.write(response, "user-import-template.xlsx", "Users",
                UserImportExcelDTO.class, sampleData);
    }

    @PostMapping("/import")
    @Operation(summary = "Import Users from Excel")
    @Parameters({
            @Parameter(name = "file", description = "Excel file", required = true),
            @Parameter(name = "updateSupport", description = "Update existing users if username matches", example = "false")
    })
    @PreAuthorize("@ss.hasPermission('system:user:import')")
    public CommonResult<UserImportRespDTO> importUsers(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport)
            throws IOException {
        List<UserImportExcelDTO> list = ExcelUtils.read(file, UserImportExcelDTO.class);
        return success(adminUserService.importUserList(list, updateSupport));
    }

}
