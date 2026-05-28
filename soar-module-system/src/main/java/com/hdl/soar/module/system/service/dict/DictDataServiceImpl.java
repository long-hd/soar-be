package com.hdl.soar.module.system.service.dict;

import com.google.common.annotations.VisibleForTesting;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataPageReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictDataPO;
import com.hdl.soar.module.system.dal.entity.dict.DictDataPO_;
import com.hdl.soar.module.system.dal.entity.dict.DictTypePO;
import com.hdl.soar.module.system.dal.postgres.dict.DictDataRepository;
import com.hdl.soar.module.system.mapper.dict.DictDataMapper;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;

/**
 * Dictionary Data Service Implementation Class
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DictDataServiceImpl implements DictDataService {

    DictDataRepository dictDataRepository;
    DictTypeService dictTypeService;

    @Override
    public Long createDictData(DictDataSaveReqDTO createReqDTO) {
        // Validate dictionary type existence
        validateDictTypeExists(createReqDTO.getDictType());

        // Validate uniqueness of the dictionary data value
        validateDictDataValueUnique(null, createReqDTO.getDictType(), createReqDTO.getValue());

        // Insert dictionary data
        DictDataPO dictData = DictDataMapper.INSTANCE.toPO(createReqDTO);
        dictDataRepository.save(dictData);

        return dictData.getId();
    }

    @Override
    public void updateDictData(DictDataSaveReqDTO updateReqDTO) {
        // Validate that the dictionary data exists
        DictDataPO existing = dictDataRepository.findById(updateReqDTO.getId())
                .orElseThrow(() -> exception(DICT_DATA_NOT_EXISTS));

        // Validate dictionary type existence
        validateDictTypeExists(updateReqDTO.getDictType());

        // Validate uniqueness of the dictionary data value
        validateDictDataValueUnique(updateReqDTO.getId(), updateReqDTO.getDictType(), updateReqDTO.getValue());

        // Update dictionary data
        DictDataMapper.INSTANCE.updateDO(updateReqDTO, existing);
        dictDataRepository.save(existing);
    }

    @Override
    public void deleteDictData(Long id) {
        dictDataRepository.findById(id).orElseThrow(() -> exception(DICT_DATA_NOT_EXISTS));
        dictDataRepository.deleteById(id);
    }

    @Override
    public void deleteDictDataList(List<Long> ids) {
        dictDataRepository.deleteAllById(ids);
    }

    @Override
    public List<DictDataPO> getDictDataList(@Nullable CommonStatusEnum status, @Nullable String dictType) {
        Specification<DictDataPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, DictDataPO_.status, status);
            eqIfPresent(predicates, cb, root, DictDataPO_.dictType, dictType);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Sort sort = Sort.by(
                Sort.Order.asc(DictDataPO_.DICT_TYPE),
                Sort.Order.asc(DictDataPO_.SORT)
                );
        return dictDataRepository.findAll(spec, sort);
    }

    @Override
    public PageResult<DictDataPO> getDictDataPage(DictDataPageReqDTO pageReqDTO) {
        Specification<DictDataPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, DictDataPO_.label, pageReqDTO.getLabel());
            eqIfPresent(predicates, cb, root, DictDataPO_.dictType, pageReqDTO.getDictType());
            eqIfPresent(predicates, cb, root, DictDataPO_.status, CommonStatusEnum.of(pageReqDTO.getStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Sort sort = Sort.by(
                Sort.Order.asc(DictDataPO_.DICT_TYPE),
                Sort.Order.asc(DictDataPO_.SORT)
        );
        Pageable pageable = PageUtils.toPageable(pageReqDTO, sort);

        Page<DictDataPO> page = dictDataRepository.findAll(spec, pageable);
        return PageUtils.toPageResult(page);
    }

    @Override
    public DictDataPO getDictData(Long id) {
        return dictDataRepository.findById(id)
                .orElseThrow(() -> exception(DICT_DATA_NOT_EXISTS));
    }

    // ===================== Utilities Class

    @VisibleForTesting
    void validateDictTypeExists(String type) {
        DictTypePO dictType = dictTypeService.getDictType(type);
        if (dictType == null) {
            throw exception(DICT_TYPE_NOT_EXISTS);
        }
        if (!CommonStatusEnum.ENABLE.equals(dictType.getStatus())) {
            throw exception(DICT_TYPE_NOT_ENABLE);
        }
    }

    @VisibleForTesting
    public void validateDictDataValueUnique(Long id, String dictType, String value) {
        DictDataPO dictData = dictDataRepository.findByDictTypeAndValue(dictType, value);
        if (dictData == null) {
            return;
        }

        // If id is null, no need to compare whether it is the same dictionary data ID
        if (id == null) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }

        if (!dictData.getId().equals(id)) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }
    }

}
