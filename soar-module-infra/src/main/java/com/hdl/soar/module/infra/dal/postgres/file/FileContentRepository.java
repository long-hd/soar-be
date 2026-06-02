package com.hdl.soar.module.infra.dal.postgres.file;

import com.hdl.soar.module.infra.dal.entity.file.FileContentPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileContentRepository extends JpaRepository<FileContentPO, Long> {

    /**
     * Latest content for a (configId, path) pair (highest id).
     */
    Optional<FileContentPO> findFirstByConfigIdAndPathOrderByIdDesc(Long configId, String path);

    /**
     * All content rows for a (configId, path) pair — used by delete (soft delete each).
     */
    List<FileContentPO> findByConfigIdAndPath(Long configId, String path);

}
