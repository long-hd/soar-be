package com.hdl.soar.module.system.service.dict;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataPageReqDTO;
import com.hdl.soar.module.system.controller.admin.dict.dto.data.DictDataSaveReqDTO;
import com.hdl.soar.module.system.dal.entity.dict.DictDataPO;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Dictionary Data Service Interface
 */
public interface DictDataService {

    /**
     * Create dictionary data
     *
     * @param createReqDTO Dictionary data information
     * @return Dictionary data ID
     */
    Long createDictData(DictDataSaveReqDTO createReqDTO);

    /**
     * Update dictionary data
     *
     * @param updateReqDTO Dictionary data information
     */
    void updateDictData(@Valid DictDataSaveReqDTO updateReqDTO);

    /**
     * Delete dictionary data
     *
     * @param id Dictionary data ID
     */
    void deleteDictData(Long id);

    /**
     * Batch delete dictionary data
     *
     * @param ids List of dictionary data IDs
     */
    void deleteDictDataList(List<Long> ids);

    /**
     * Get dictionary data list
     *
     * @param status   Status
     * @param dictType Dictionary type
     * @return Full list of dictionary data
     */
    List<DictDataPO> getDictDataList(@Nullable CommonStatusEnum status, @Nullable String dictType);

    /**
     * Get paginated list of dictionary data
     *
     * @param pageReqDTO pagination request
     * @return paginated list of dictionary data
     */
    PageResult<DictDataPO> getDictDataPage(@Valid DictDataPageReqDTO pageReqDTO);

    /**
     * Get dictionary data details
     *
     * @param id dictionary data ID
     * @return dictionary data
     */
    DictDataPO getDictData(Long id);
}
