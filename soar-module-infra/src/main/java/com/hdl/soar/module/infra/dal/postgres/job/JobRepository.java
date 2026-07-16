package com.hdl.soar.module.infra.dal.postgres.job;

import com.hdl.soar.module.infra.dal.entity.job.JobPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<JobPO, Long>, JpaSpecificationExecutor<JobPO> {

    Optional<JobPO> findByHandlerName(String handlerName);

}
