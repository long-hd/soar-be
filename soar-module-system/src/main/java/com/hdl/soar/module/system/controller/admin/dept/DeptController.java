package com.hdl.soar.module.system.controller.admin.dept;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptListReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptRespDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO;
import com.hdl.soar.module.system.mapper.dept.DeptMapper;
import com.hdl.soar.module.system.service.dept.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Management - Department")
@Validated
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeptController {

    DeptService deptService;

    @PostMapping("/create")
    @Operation(summary = "Create Department")
    @PreAuthorize("@ss.hasPermission('system:dept:create')")
    public CommonResult<Long> createDept(@Valid @RequestBody DeptSaveReqDTO createReqDTO) {
        return success(deptService.createDept(createReqDTO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update Department")
    @PreAuthorize("@ss.hasPermission('system:dept:update')")
    public CommonResult<Boolean> updateDept(@Valid @RequestBody DeptSaveReqDTO updateReqDTO) {
        deptService.updateDept(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete Department")
    @Parameter(name = "id", description = "Department ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dept:delete')")
    public CommonResult<Boolean> deleteDept(@RequestParam("id") Long id) {
        deptService.deleteDept(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "Batch Delete Departments")
    @Parameter(name = "ids", description = "Department ID list", required = true)
    @PreAuthorize("@ss.hasPermission('system:dept:delete')")
    public CommonResult<Boolean> deleteDeptList(@RequestParam("ids") List<Long> ids) {
        deptService.deleteDeptList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get Department")
    @Parameter(name = "id", description = "Department ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public CommonResult<DeptRespDTO> getDept(@RequestParam("id") Long id) {
        DeptPO dept = deptService.getDept(id);
        return success(DeptMapper.INSTANCE.toDTO(dept));
    }

    @GetMapping("/list")
    @Operation(summary = "Get Department list")
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public CommonResult<List<DeptRespDTO>> getDeptList(DeptListReqDTO reqDTO) {
        List<DeptPO> list = deptService.getDeptList(reqDTO);
        return success(DeptMapper.INSTANCE.toDTOList(list));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "Get simple department list",
            description = "Only enabled departments, used for dropdown")
    public CommonResult<List<DeptSimpleRespDTO>> getSimpleDeptList() {
        List<DeptPO> list = deptService.getDeptListByStatus(CommonStatusEnum.ENABLE);
        list.sort(Comparator.comparing(DeptPO::getSort));
        return success(DeptMapper.INSTANCE.toSimpleDTOList(list));
    }

}
