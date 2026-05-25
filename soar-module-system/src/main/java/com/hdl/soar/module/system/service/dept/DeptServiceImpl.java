package com.hdl.soar.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO;
import com.hdl.soar.module.system.dal.postgres.dept.DeptRepository;
import com.hdl.soar.module.system.dal.redis.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.*;

/**
 * Department Service implementation class
 */
@Slf4j
@Service
@Validated
public class DeptServiceImpl implements DeptService {

    DeptRepository deptRepository;

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
    // @DataPermission(enable = false) // Disable data permission to avoid creating incorrect cache entries
    @Cacheable(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, key = "#deptId")
    public Set<Long> getChildDeptIdsFromCache(Long deptId) {
        List<DeptPO> children = getChildDeptList(deptId);
        return convertSet(children, DeptPO::getId);
    }

}
