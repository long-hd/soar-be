package com.hdl.soar.module.system.dal.postgres.permission;

import com.hdl.soar.module.system.dal.entity.permission.UserRolePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRolePO, Long> {
    @Query("select ur.roleId from UserRolePO ur where ur.userId = :userId")
    Set<Long> findRoleIdsByUserId (@Param("userId") Long userId);

    List<UserRolePO> findAllByRoleIdIn(Collection<Long> roleIds);

    void deleteByRoleId(Long roleId);

    void deleteByUserId(Long userId);

    List<UserRolePO> findByUserId(Long userId);

    void deleteByUserIdAndRoleIdIn(Long userId, Collection<Long> roleIds);
}
