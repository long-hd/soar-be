package com.hdl.soar.framework.common.biz.system.dict;

import com.hdl.soar.framework.common.biz.system.dict.dto.DictDataRespDTO;

import java.util.List;

/**
 * Dict data API interface — used by framework modules (e.g. Excel, validation)
 * to look up dictionary data without depending on module-system directly.
 * <p>
 * Implemented by module-system's DictDataApiImpl.
 */
public interface DictDataCommonApi {

    /**
     * Get all dict data entries for the given dict type.
     *
     * @param dictType dict type code (e.g. "sys_common_status")
     * @return list of dict data entries
     */
    List<DictDataRespDTO> getDictDataList(String dictType);

}