package com.hdl.soar.module.system.api.dict;

import cn.hutool.core.collection.CollUtil;
import com.hdl.soar.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.util.collection.CollectionUtils;
import com.hdl.soar.module.system.dal.entity.dict.DictDataPO;
import com.hdl.soar.module.system.dal.entity.dict.DictDataPO_;
import com.hdl.soar.module.system.dal.postgres.dict.DictDataRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DictDataApiImpl implements DictDataApi {

    DictDataRepository dictDataRepository;

    @Override
    public List<DictDataRespDTO> getDictDataList(String dictType) {
        List<DictDataPO> dataList = dictDataRepository.findByDictTypeAndStatus(
                dictType, CommonStatusEnum.ENABLE, Sort.by(Sort.Direction.ASC, DictDataPO_.SORT));
        return dataList.stream()
                .map(this::toDTO)
                .toList();
    }

    private DictDataRespDTO toDTO(DictDataPO po) {
        DictDataRespDTO dto = new DictDataRespDTO();
        dto.setLabel(po.getLabel());
        dto.setValue(po.getValue());
        dto.setDictType(po.getDictType());
        dto.setStatus(po.getStatus() != null ? po.getStatus().getStatus() : null);
        return dto;
    }

    @Override
    public void validateDictDataList(String dictType, Collection<String> values) {
        if (CollUtil.isEmpty(values)) {
            return;
        }

        Map<String, DictDataPO> dictDataMap = CollectionUtils.convertMap(
                dictDataRepository.findByDictTypeAndValueIn(dictType, values),
                DictDataPO::getValue
        );

        // Validation
        values.forEach(value -> {
            DictDataPO dictData = dictDataMap.get(value);

            if (dictData == null) {
                throw exception(DICT_DATA_NOT_EXISTS);
            }

            if (!CommonStatusEnum.ENABLE.equals(dictData.getStatus())) {
                throw exception(DICT_DATA_NOT_ENABLE, dictData.getLabel());
            }
        });
    }
}
