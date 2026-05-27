package com.hdl.soar.module.system.dal.postgres.dict;

import com.hdl.soar.module.system.dal.entity.dict.DictDataPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DictDataRepository extends JpaRepository<DictDataPO, Long>, JpaSpecificationExecutor<DictDataPO> {
}
