package com.hdl.soar.module.infra.dal.postgres.file;

import com.hdl.soar.module.infra.dal.entity.file.FileConfigPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileConfigRepository extends JpaRepository<FileConfigPO, Long>,
        JpaSpecificationExecutor<FileConfigPO> {

    /**
     * Find the master (default) config.
     */
    Optional<FileConfigPO> findByMasterTrue();

    /**
     * Find all master configs except the given id (used to demote others when setting a new master).
     */
    List<FileConfigPO> findByMasterTrueAndIdNot(Long id);

}
