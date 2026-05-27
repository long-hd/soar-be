package com.hdl.soar.module.system.service.dict;

import cn.hutool.core.util.StrUtil;
import com.google.common.annotations.VisibleForTesting;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictTypePO;
import com.hdl.soar.module.system.dal.postgres.dict.DictTypeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.module.system.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DictTypeServiceImpl implements DictTypeService {

    DictTypeRepository dictTypeRepository;

    @Override
    public Long createDictType(DictTypeSaveReqDTO createReqDTO) {
        // Validate the uniqueness of the dictionary type name
        validateDictTypeNameUnique(null, createReqDTO.getName());

        // Validate the uniqueness of the dictionary type's type
        validateDictTypeUnique(null, createReqDTO.getType());

        // Insert dictionary type
        DictTypePO dictType = DictTypePO.builder()
                .name(createReqDTO.getName())
                .type(createReqDTO.getType())
                .status(CommonStatusEnum.of(createReqDTO.getStatus()))
                .remark(createReqDTO.getRemark())
                .build();
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
        DictTypePO dictType = existing.toBuilder()
                .name(updateReqDTO.getName())
                .type(updateReqDTO.getType())
                .status(CommonStatusEnum.of(updateReqDTO.getStatus()))
                .remark(updateReqDTO.getRemark())
                .build();
        dictTypeRepository.save(dictType);
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

}
