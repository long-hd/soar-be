package com.hdl.soar.module.infra.dal.postgres.file;

import com.hdl.soar.module.infra.dal.entity.file.FilePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FilePO, Long>,
        JpaSpecificationExecutor<FilePO> {
}
