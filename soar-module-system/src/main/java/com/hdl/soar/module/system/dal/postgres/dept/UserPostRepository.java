package com.hdl.soar.module.system.dal.postgres.dept;

import com.hdl.soar.module.system.dal.entity.dept.UserPostPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserPostRepository extends JpaRepository<UserPostPO, Long> {
    List<UserPostPO> findAllByUserId(Long userId);

    void deleteByUserIdAndPostIdIn(Long userId, Collection<Long> postIds);

    void deleteByUserId(Long userId);
}
