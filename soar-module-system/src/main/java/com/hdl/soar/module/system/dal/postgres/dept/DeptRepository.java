package com.hdl.soar.module.system.dal.postgres.dept;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.dal.entity.dept.DeptPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeptRepository extends JpaRepository<DeptPO, Long>, JpaSpecificationExecutor<DeptPO> {
    List<DeptPO> findAllByParentId(Long parentId);

    List<DeptPO> findAllByParentIdIn(Collection<Long> parentIds);

    Optional<DeptPO> findByParentIdAndName(Long parentId, String name);

    double countByParentId(Long parentId);

    List<DeptPO> findAllByStatus(CommonStatusEnum status);
}
