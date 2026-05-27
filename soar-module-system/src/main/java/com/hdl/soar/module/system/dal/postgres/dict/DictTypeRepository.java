package com.hdl.soar.module.system.dal.postgres.dict;

import com.hdl.soar.module.system.dal.entity.dict.DictTypePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DictTypeRepository extends JpaRepository<DictTypePO, Long>, JpaSpecificationExecutor<DictTypePO> {
}
