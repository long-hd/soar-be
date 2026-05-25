package com.hdl.soar.module.system.service.dept;

import com.hdl.soar.module.system.dal.entity.dept.DeptPO;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Department Service interface
 */
public interface DeptService {

    /**
     * Get all child departments for the specified departments
     *
     * @param ids collection of department IDs
     * @return list of child departments
     */
    List<DeptPO> getChildDeptList(Collection<Long> ids);

    /**
     * Get all child departments of the specified department
     *
     * @param id department ID
     * @return list of child departments
     */
    default List<DeptPO> getChildDeptList(Long id) {
        return getChildDeptList(Collections.singleton(id));
    }

    /**
     * Get all child departments from cache
     *
     * @param deptId parent department ID
     * @return set of child department IDs
     */
    Set<Long> getChildDeptIdsFromCache(Long deptId);

}
