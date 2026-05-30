package com.hdl.soar.module.system.service.dept;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostPageReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dept.PostPO;
import com.hdl.soar.module.system.dal.postgres.dept.PostRepository;
import com.hdl.soar.module.system.mapper.dept.PostMapper;
import com.hdl.soar.module.system.dal.entity.dept.PostPO_;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostServiceImpl implements PostService {

    PostRepository postRepository;

    @Override
    public Long createPost(PostSaveReqDTO createReqDTO) {
        // Validate name and code unique
        validatePostNameUnique(null, createReqDTO.getName());
        validatePostCodeUnique(null, createReqDTO.getCode());

        // Insert position
        PostPO post = PostMapper.INSTANCE.toPO(createReqDTO);
        postRepository.save(post);
        return post.getId();
    }

    @Override
    public void updatePost(PostSaveReqDTO updateReqDTO) {
        // Validate post exist
        PostPO existing = postRepository.findById(updateReqDTO.getId())
                .orElseThrow(() -> exception(POST_NOT_FOUND));

        // Validate name and code unique
        validatePostNameUnique(null, updateReqDTO.getName());
        validatePostCodeUnique(null, updateReqDTO.getCode());

        // Update position
        PostMapper.INSTANCE.updatePO(updateReqDTO, existing);
        postRepository.save(existing);
    }

    @Override
    public void deletePost(Long id) {
        postRepository.findById(id)
                .orElseThrow(() -> exception(POST_NOT_FOUND));
        postRepository.deleteById(id);
    }

    @Override
    public void deletePostList(List<Long> ids) {
        postRepository.deleteAllById(ids);
    }

    @Override
    public PostPO getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> exception(POST_NOT_FOUND));
    }

    @Override
    public PageResult<PostPO> getPostPage(PostPageReqDTO pageReqDTO) {
        Specification<PostPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, PostPO_.name, pageReqDTO.getName());
            likeIfPresent(predicates, cb, root, PostPO_.code, pageReqDTO.getCode());
            eqIfPresent(predicates, cb, root, PostPO_.status, CommonStatusEnum.of(pageReqDTO.getStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable =  PageUtils.toPageable(pageReqDTO);
        Page<PostPO> page = postRepository.findAll(spec, pageable);

        return PageUtils.toPageResult(page);
    }

    @Override
    public List<PostPO> getPostListByStatus(CommonStatusEnum status) {
        return postRepository.findAllByStatus(status);
    }

    // ================ Utilities method

    private void validatePostNameUnique(Long id, String name) {
        Optional<PostPO> post = postRepository.findByName(name);
        if (post.isEmpty()) {
            return;
        }
        // If id is null, no need to check whether it is the same post
        if (id == null || !post.get().getId().equals(id)) {
            throw exception(POST_NAME_DUPLICATE);
        }
    }

    private void validatePostCodeUnique(Long id, String code) {
        Optional<PostPO> post = postRepository.findByCode(code);
        if (post.isEmpty()) {
            return;
        }
        // If id is null, no need to check whether it is the same post
        if (id == null || !post.get().getId().equals(id)) {
            throw exception(POST_CODE_DUPLICATE);
        }
    }

}
