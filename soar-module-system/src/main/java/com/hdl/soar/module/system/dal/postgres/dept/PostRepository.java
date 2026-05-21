package com.hdl.soar.module.system.dal.postgres.dept;

import com.hdl.soar.module.system.dal.entity.dept.PostPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<PostPO, Long> {
}
