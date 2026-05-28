package com.hdl.soar.module.system.service.dict;

import cn.hutool.core.util.StrUtil;
import com.google.common.annotations.VisibleForTesting;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.framework.jpa.core.util.SpecUtils;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypePageReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictTypePO;
import com.hdl.soar.module.system.dal.postgres.dict.DictDataRepository;
import com.hdl.soar.module.system.dal.postgres.dict.DictTypeRepository;
import com.hdl.soar.module.system.mapper.dict.DictTypeMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.Instant;
import com.hdl.soar.module.system.dal.entity.dict.DictTypePO_;

import java.util.ArrayList;
import java.util.List;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DictTypeServiceImpl implements DictTypeService {

    DictTypeRepository dictTypeRepository;
    DictDataRepository dictDataRepository;

    @Override
    public Long createDictType(DictTypeSaveReqDTO createReqDTO) {
        // Validate the uniqueness of the dictionary type name
        validateDictTypeNameUnique(null, createReqDTO.getName());

        // Validate the uniqueness of the dictionary type's type
        validateDictTypeUnique(null, createReqDTO.getType());

        // Insert dictionary type
        DictTypePO dictType = DictTypeMapper.INSTANCE.toPO(createReqDTO);
        dictTypeRepository.save(dictType);

        return dictType.getId();
    }

    @Override
    public void updateDictType(DictTypeSaveReqDTO updateReqDTO) {
        // Validate that the dictionary type exists
        DictTypePO existing = validateDictTypeExists(updateReqDTO.getId());

        // Validate the uniqueness of the dictionary type name
        validateDictTypeNameUnique(updateReqDTO.getId(), updateReqDTO.getName());

        // Validate the uniqueness of the dictionary type's type
        validateDictTypeUnique(updateReqDTO.getId(), updateReqDTO.getType());

        // Update dictionary type
        DictTypeMapper.INSTANCE.updatePO(updateReqDTO, existing);
        dictTypeRepository.save(existing);
    }

    @Override
    public void deleteDictType(Long id) {
        // 1. Validate exists
        DictTypePO dictType = validateDictTypeExists(id);
        // 2. Validate no dict data under this type
        validateDictTypeHasNoChildren(dictType.getType());
        // 3. Soft delete (JPA @SoftDelete handles it)
        dictTypeRepository.deleteById(id);
    }

    @Override
    public void deleteDictTypeList(List<Long> ids) {
        // 1. Validate all exist
        List<DictTypePO> dictTypes = dictTypeRepository.findAllById(ids);
        if(dictTypes.size() != ids.size()) {
            throw exception(DICT_TYPE_NOT_EXISTS);
        }
        // 2. Validate none have children
        dictTypes.forEach(dt -> validateDictTypeHasNoChildren(dt.getType()));
        // 3. Batch soft delete
        dictTypeRepository.deleteAllById(ids);
    }

    @Override
    public PageResult<DictTypePO> getDictTypePage(DictTypePageReqDTO pageReqDTO) {
        // 1. Build Specification
        Specification<DictTypePO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            SpecUtils.likeIfPresent(predicates, cb, root, DictTypePO_.name, pageReqDTO.getName());
            SpecUtils.likeIfPresent(predicates, cb, root, DictTypePO_.type, pageReqDTO.getType());
            SpecUtils.eqIfPresent(predicates, cb, root, DictTypePO_.status,
                    CommonStatusEnum.of(pageReqDTO.getStatus()));
            SpecUtils.betweenIfPresent(predicates, cb, root, DictTypePO_.createTime,
                    new Instant[]{pageReqDTO.getCreateTimeStart(), pageReqDTO.getCreateTimeEnd()});
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 2. Build Pageable (pageNo is 1-based, Spring is 0-based)
        Pageable pageable = PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, DictTypePO_.CREATE_TIME));

        // 3. Query
        Page<DictTypePO> page = dictTypeRepository.findAll(spec, pageable);
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public DictTypePO getDictType(Long id) {
        return dictTypeRepository.findById(id).orElse(null);
    }

    @Override
    public DictTypePO getDictType(String type) {
        return dictTypeRepository.findFirstByType(type).orElse(null);
    }

    @Override
    public List<DictTypePO> getDictTypeList() {
        return dictTypeRepository.findAll();
    }

    // ================ Utilities method

    @VisibleForTesting
    void validateDictTypeNameUnique(Long id, String name) {
        DictTypePO dictType = dictTypeRepository.findFirstByName(name).orElse(null);
        if (dictType == null) {
            return;
        }

        // If id is null, no need to compare whether it is the same dictionary type ID
        if (id == null) {
            throw exception(DICT_TYPE_NAME_DUPLICATE);
        }

        if (!dictType.getId().equals(id)) {
            throw exception(DICT_TYPE_NAME_DUPLICATE);
        }
    }

    @VisibleForTesting
    void validateDictTypeUnique(Long id, String type) {
        if (StrUtil.isEmpty(type)) {
            return;
        }

        DictTypePO dictType = dictTypeRepository.findFirstByType(type).orElse(null);
        if (dictType == null) {
            return;
        }

        // If id is null, no need to compare whether it is the same dictionary type ID
        if (id == null) {
            throw exception(DICT_TYPE_TYPE_DUPLICATE);
        }

        if (!dictType.getId().equals(id)) {
            throw exception(DICT_TYPE_TYPE_DUPLICATE);
        }
    }

    @VisibleForTesting
    DictTypePO validateDictTypeExists(Long id) {
        return dictTypeRepository.findById(id)
                .orElseThrow(() -> exception(DICT_TYPE_NOT_EXISTS));
    }

    @VisibleForTesting
    void validateDictTypeHasNoChildren(String type) {
        if(dictDataRepository.countByDictType(type) > 0) {
            throw exception(DICT_TYPE_HAS_CHILDREN);
        }
    }
}
