package com.hdl.soar.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptListReqDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Department Service interface
 */
public interface DeptService {

    /**
     * Create a department
     *
     * @param createReqDTO Department information
     * @return Department ID
     */
    Long createDept(DeptSaveReqDTO createReqDTO);

    /**
     * Update department
     *
     * @param updateReqDTO Department information
     */
    void updateDept(DeptSaveReqDTO updateReqDTO);

    /**
     * Delete a department
     *
     * @param id Department ID
     */
    void deleteDept(Long id);

    /**
     * Delete multiple departments
     *
     * @param ids List of department IDs
     */
    void deleteDeptList(List<Long> ids);

    /**
     * Get department information
     *
     * @param id Department ID
     * @return Department information
     */
    DeptPO getDept(Long id);

    /**
     * Get department information list
     *
     * @param ids department ID list
     * @return department information list
     */
    List<DeptPO> getDeptList(Collection<Long> ids);

    /**
     * Retrieves a list of departments based on filtering conditions.
     *
     * @param reqDTO the request object containing filter conditions
     * @return the list of departments
     */
    List<DeptPO> getDeptList(DeptListReqDTO reqDTO);

    /**
     * Get department map by given IDs
     *
     * @param ids department ID list
     * @return department map
     */
    default Map<Long, DeptPO> getDeptMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<DeptPO> list = getDeptList(ids);
        return list.stream().collect(Collectors.toMap(DeptPO::getId, d -> d));
    }


    /**
     * Retrieves a list of departments based on status.
     *
     * @param status dept status
     * @return the list of departments
     */
    List<DeptPO> getDeptListByStatus(CommonStatusEnum status);

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
