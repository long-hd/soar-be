package com.hdl.soar.module.system.controller.admin.dept;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageParam;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.excel.core.util.ExcelUtils;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostPageReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostRespDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostSaveReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostSimpleRespDTO;
import com.hdl.soar.module.system.dal.entity.dept.PostPO;
import com.hdl.soar.module.system.mapper.dept.PostMapper;
import com.hdl.soar.module.system.service.dept.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Job Position Management")
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {

    PostService postService;

    @PostMapping("/create")
    @Operation(summary = "Create position")
    @PreAuthorize("@ss.hasPermission('system:post:create')")
    public CommonResult<Long> createPost(@Valid @RequestBody PostSaveReqDTO createReqDTO) {
        Long postId = postService.createPost(createReqDTO);
        return success(postId);
    }

    @PutMapping("/update")
    @Operation(summary = "Update position")
    @PreAuthorize("@ss.hasPermission('system:post:update')")
    public CommonResult<Boolean> updatePost(@Valid @RequestBody PostSaveReqDTO updateReqDTO) {
        postService.updatePost(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete position")
    @PreAuthorize("@ss.hasPermission('system:post:delete')")
    public CommonResult<Boolean> deletePost(@RequestParam("id") Long id) {
        postService.deletePost(id);
        return success(true);
    }

    @DeleteMapping("delete-list")
    @Operation(summary = "Batch delete positions")
    @PreAuthorize("@ss.hasPermission('system:post:delete')")
    public CommonResult<Boolean> deletePostList(@RequestParam("ids") List<Long> ids) {
        postService.deletePostList(ids);
        return success(true);
    }

    @GetMapping(value = "/get")
    @Operation(summary = "Get position information")
    @Parameter(name = "id", description = "Position ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:post:query')")
    public CommonResult<PostRespDTO> getPost(@RequestParam("id") Long id) {
        PostPO post = postService.getPost(id);
        return success(PostMapper.INSTANCE.toDTO(post));
    }

    @GetMapping("/page")
    @Operation(summary = "Get position paginated list")
    @PreAuthorize("@ss.hasPermission('system:post:query')")
    public CommonResult<PageResult<PostRespDTO>> getPostPage(@Valid PostPageReqDTO pageReqDTO) {
        PageResult<PostPO> pageResult = postService.getPostPage(pageReqDTO);
        return success(new PageResult<>(
                PostMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()
        ));
    }

    @GetMapping(value = {"/list-all-simple", "simple-list"})
    @Operation(summary = "Get full position list", description = "Only includes enabled positions, mainly used for frontend dropdowns")
    public CommonResult<List<PostSimpleRespDTO>> getSimplePostList() {
        // Get position list, only enabled ones
        List<PostPO> list = postService.getPostListByStatus(CommonStatusEnum.ENABLE);
        // Sort and return to frontend
        list.sort(Comparator.comparing(PostPO::getSort));
        return success(PostMapper.INSTANCE.toSimpleDTOList(list));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Position management export")
    @PreAuthorize("@ss.hasPermission('system:post:export')")
    // TODO: @ApiAccessLog(operateType = EXPORT)
    public void export(HttpServletResponse response, @Valid PostPageReqDTO reqDTO) throws IOException {
        reqDTO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PostPO> list = postService.getPostPage(reqDTO).getList();
        // Export
        ExcelUtils.write(response,
                "Position data.xls",
                "Position list",
                PostRespDTO.class,
                PostMapper.INSTANCE.toDTOList(list));
    }

}
