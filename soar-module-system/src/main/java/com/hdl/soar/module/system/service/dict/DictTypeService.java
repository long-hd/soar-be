package com.hdl.soar.module.system.service.dict;

import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeSaveReqDTO;
import jakarta.validation.Valid;

/**
 * Dictionary Type Service Interface
 */
public interface DictTypeService {

    /**
     * Create a dictionary type
     *
     * @param createReqDTO dictionary type information
     * @return dictionary type ID
     */
    Long createDictType(DictTypeSaveReqDTO createReqDTO);

    /**
     * Update dictionary type
     *
     * @param updateReqDTO dictionary type information
     */
    void updateDictType(DictTypeSaveReqDTO updateReqDTO);
}
