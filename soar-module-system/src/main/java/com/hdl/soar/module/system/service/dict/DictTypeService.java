package com.hdl.soar.module.system.service.dict;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypePageReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.type.DictTypeSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictTypePO;

import java.util.List;

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

    /**
     * Delete dictionary type
     *
     * @param id Dictionary type ID
     */
    void deleteDictType(Long id);

    /**
     * Batch delete dictionary types
     *
     * <p>Admin should delete all **DictData** entries before deleting the **DictType**.
     * @param ids List of dictionary type IDs
     */
    void deleteDictTypeList(List<Long> ids);

    /**
     * Get paginated list of dictionary types
     *
     * @param pageReqDTO Page request
     * @return Paginated list of dictionary types
     */
    PageResult<DictTypePO> getDictTypePage(DictTypePageReqDTO pageReqDTO);

    /**
     * Get dictionary type details
     *
     * @param id Dictionary type ID
     * @return Dictionary type
     */
    DictTypePO getDictType(Long id);

    /**
     * Get dictionary type details
     *
     * @param type Dictionary type
     * @return Dictionary type details
     */
    DictTypePO getDictType(String type);

    /**
     * Get all dictionary type list
     *
     * @return List of dictionary types
     */
    List<DictTypePO> getDictTypeList();
}
