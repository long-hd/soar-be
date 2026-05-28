package com.hdl.soar.module.system.dal.postgres.dict;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.dal.entity.dict.DictDataPO;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DictDataRepository extends JpaRepository<DictDataPO, Long>, JpaSpecificationExecutor<DictDataPO> {
    long countByDictType(String dictType);

    List<DictDataPO> findByDictTypeAndStatus(String dictType, CommonStatusEnum status, Sort sort);

    List<DictDataPO> findByDictTypeAndValueIn(String dictType, Collection<String> values);

    DictDataPO findByDictTypeAndValue(String dictType, String value);
}
