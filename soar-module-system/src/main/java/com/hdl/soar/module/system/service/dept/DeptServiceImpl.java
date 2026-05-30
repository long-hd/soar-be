package com.hdl.soar.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import com.google.common.annotations.VisibleForTesting;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptListReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO_;
import com.hdl.soar.module.system.dal.postgres.dept.DeptRepository;
import com.hdl.soar.module.system.dal.redis.RedisKeyConstants;
import com.hdl.soar.module.system.mapper.dept.DeptMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.*;
import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;

/**
 * Department Service implementation class
 */
@Slf4j
@Service
@Validated
public class DeptServiceImpl implements DeptService {

    DeptRepository deptRepository;

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true)
    public Long createDept(DeptSaveReqDTO createReqDTO) {
        // Default parent to root
        if (createReqDTO.getParentId() == null) {
            createReqDTO.setParentId(DeptPO.PARENT_ID_ROOT);
        }
        // 1. Validate
        validateParentDept(null, createReqDTO.getParentId());
        validateDeptNameUnique(null, createReqDTO.getParentId(), createReqDTO.getName());

        // 2. Save
        DeptPO dept = DeptMapper.INSTANCE.toPO(createReqDTO);
        deptRepository.save(dept);
        return dept.getId();
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true)
    public void updateDept(DeptSaveReqDTO updateReqDTO) {
        if (updateReqDTO.getParentId() == null) {
            updateReqDTO.setParentId(DeptPO.PARENT_ID_ROOT);
        }
        // 1. Validate exists
        DeptPO existing = deptRepository.findById(updateReqDTO.getId())
                .orElseThrow(() -> exception(DEPT_NOT_FOUND));
        // 2. Validate parent + name unique
        validateParentDept(updateReqDTO.getId(), updateReqDTO.getParentId());
        validateDeptNameUnique(updateReqDTO.getId(), updateReqDTO.getParentId(), updateReqDTO.getName());

        // 3. Update
        DeptMapper.INSTANCE.updatePO(updateReqDTO, existing);
        deptRepository.save(existing);
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true)
    public void deleteDept(Long id) {
        // 1. Validate exists
        deptRepository.findById(id)
                .orElseThrow(() -> exception(DEPT_NOT_FOUND));
        // 2. Validate no children
        if (deptRepository.countByParentId(id) > 0) {
            throw exception(DEPT_EXITS_CHILDREN);
        }
        // 3. Delete
        deptRepository.deleteById(id);
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true)
    public void deleteDeptList(List<Long> ids) {
        // 1. Validate all exist
        List<DeptPO> depts = deptRepository.findAllById(ids);
        if (depts.size() != ids.size()) {
            throw exception(DEPT_NOT_FOUND);
        }
        // 2. Validate none have children
        for (Long id : ids) {
            if (deptRepository.countByParentId(id) > 0) {
                throw exception(DEPT_EXITS_CHILDREN);
            }
        }
        // 3. Delete
        deptRepository.deleteAllById(ids);
    }

    @Override
    public DeptPO getDept(Long id) {
        return deptRepository.findById(id)
                .orElseThrow(() -> exception(DEPT_NOT_FOUND));
    }

    @Override
    public List<DeptPO> getDeptList(Collection<Long> ids) {
        return deptRepository.findAllById(ids);
    }

    @Override
    public List<DeptPO> getDeptList(DeptListReqDTO reqDTO) {
        Specification<DeptPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, DeptPO_.name, reqDTO.getName());
            eqIfPresent(predicates, cb, root, DeptPO_.status, CommonStatusEnum.of(reqDTO.getStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Sort.Order order = Sort.Order.asc(DeptPO_.SORT);
        return deptRepository.findAll(spec, Sort.by(order));
    }

    @Override
    public List<DeptPO> getDeptListByStatus(CommonStatusEnum status) {
        return deptRepository.findAllByStatus(status);
    }

    @Override
    public List<DeptPO> getChildDeptList(Collection<Long> ids) {
        List<DeptPO> children = new LinkedList<>();
        // Iterate through each level
        Collection<Long> parentIds = ids;

        for (int i = 0; i < Short.MAX_VALUE; i++) { // Use Short.MAX_VALUE to avoid infinite loops in edge cases
            // Query all child departments at the current level
            List<DeptPO> depts = deptRepository.findAllByParentIdIn(parentIds);

            // 1. If no child departments, stop iteration
            if (CollUtil.isEmpty(depts)) {
                break;
            }

            // 2. If child departments exist, continue traversal
            children.addAll(depts);
            parentIds = convertSet(depts, DeptPO::getId);
        }

        return children;
    }

    @Override
    // TODO: @DataPermission(enable = false) // Disable data permission to avoid creating incorrect cache entries
    @Cacheable(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, key = "#deptId")
    public Set<Long> getChildDeptIdsFromCache(Long deptId) {
        List<DeptPO> children = getChildDeptList(deptId);
        return convertSet(children, DeptPO::getId);
    }

    // ============== Utilities method

    @VisibleForTesting
    void validateParentDept(Long id, Long parentId) {
        if (parentId == null || DeptPO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. Cannot set self as parent
        if (parentId.equals(id)) {
            throw exception(DEPT_PARENT_ERROR);
        }
        // 2. Parent must exist
        DeptPO parent = deptRepository.findById(parentId)
                .orElseThrow(() -> exception(DEPT_PARENT_NOT_EXITS));
        // 3. Circular check — walk up the tree, if we meet ourselves -> cycle
        if (id == null) { // Create mode, no cycle possible
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            Long nextParentId = parent.getParentId();
            if (nextParentId == null || DeptPO.PARENT_ID_ROOT.equals(nextParentId)) {
                break; // Reached root, no cycle
            }
            if (id.equals(nextParentId)) {
                throw exception(DEPT_PARENT_IS_CHILD);
            }
            parent = deptRepository.findById(nextParentId)
                    .orElse(null);
            if (parent == null) {
                break;
            }
        }
    }

    @VisibleForTesting
    void validateDeptNameUnique(Long id, Long parentId, String name) {
        Optional<DeptPO> existing = deptRepository.findByParentIdAndName(parentId, name);
        if (existing.isEmpty()) {
            return;
        }
        if (id == null || !existing.get().getId().equals(id)) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
    }

}
