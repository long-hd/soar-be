package com.hdl.soar.module.system.api.dict;

import com.hdl.soar.framework.common.biz.system.dict.DictDataCommonApi;

import java.util.Collection;

/**
 * Dictionary Data API Interface
 */
public interface DictDataApi extends DictDataCommonApi {

    /**
     * Validates whether the dictionary data entries are valid.
     * <p>The following cases are considered invalid:<br>
     * 1. The dictionary data does not exist<br>
     * 2. The dictionary data is disabled
     *
     * @param dictType the dictionary type
     * @param values   the collection of dictionary data values
     */
    void validateDictDataList(String dictType, Collection<String> values);

}
