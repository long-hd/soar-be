package com.hdl.soar.module.system.dal.postgres.dept;

import com.hdl.soar.module.system.dal.entity.dept.DeptPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DeptRepository extends JpaRepository<DeptPO, Long> {
    List<DeptPO> findAllByParentId(Long parentId);

    List<DeptPO> findAllByParentIdIn(Collection<Long> parentIds);
}
