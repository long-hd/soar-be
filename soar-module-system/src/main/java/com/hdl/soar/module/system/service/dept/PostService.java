package com.hdl.soar.module.system.service.dept;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostPageReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dept.PostPO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * Position Service interface
 */
public interface PostService {

    /**
     * Create position
     *
     * @param createReqDTO position information
     * @return position ID
     */
    Long createPost(PostSaveReqDTO createReqDTO);

    /**
     * Update position
     *
     * @param updateReqDTO position information
     */
    void updatePost(PostSaveReqDTO updateReqDTO);

    /**
     * Delete position information
     *
     * @param id position ID
     */
    void deletePost(Long id);

    /**
     * Batch delete position information
     *
     * @param ids position ID list
     */
    void deletePostList(List<Long> ids);

    /**
     * Get position information
     *
     * @param id position ID
     * @return position information
     */
    PostPO getPost(Long id);

    /**
     * Get post list by IDs
     *
     * @param ids post IDs
     * @return post list
     */
    List<PostPO> getPostList(Collection<Long> ids);

    /**
     * Get position paginated list
     *
     * @param pageReqDTO pagination conditions
     * @return position paginated list
     */
    PageResult<PostPO> getPostPage(PostPageReqDTO pageReqDTO);

    /**
     * Get position list base on status
     *
     * @param status post status
     * @return the position list
     */
    List<PostPO> getPostListByStatus(CommonStatusEnum status);

}
