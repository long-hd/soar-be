package com.hdl.soar.module.system.dal.postgres.dept;

import com.hdl.soar.module.system.dal.entity.dept.UserPostPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPostRepository extends JpaRepository<UserPostPO, Long> {
}
