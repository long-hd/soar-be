package com.hdl.soar.module.system.dal.postgres.dept;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.dal.entity.dept.PostPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<PostPO, Long>, JpaSpecificationExecutor<PostPO> {
    Optional<PostPO> findByName(String name);

    Optional<PostPO> findByCode(String code);

    List<PostPO> findAllByStatus(CommonStatusEnum status);
}
