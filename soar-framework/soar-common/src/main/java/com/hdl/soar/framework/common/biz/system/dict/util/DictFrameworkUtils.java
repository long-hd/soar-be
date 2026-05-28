package com.hdl.soar.framework.common.biz.system.dict.util;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hdl.soar.framework.common.biz.system.dict.DictDataCommonApi;
import com.hdl.soar.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.hdl.soar.framework.common.util.cache.CacheUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Dict data utility — provides static lookups for dict label/value conversion.
 *
 * <p>Used by:
 * <ul>
 *   <li>Excel {@code DictConvert} — convert Integer → label when exporting</li>
 *   <li>{@code @InDict} validator — validate incoming values against dict</li>
 *   <li>{@code SelectSheetWriteHandler} — populate dropdown options in Excel</li>
 * </ul>
 *
 * <p>Internally caches dict data per type with 1-minute async refresh via Guava LoadingCache.
 */
@Slf4j
public class DictFrameworkUtils {

    private static DictDataCommonApi dictDataApi;

    /**
     * Cache: dictType → List<DictDataRespDTO>
     */
    private static final LoadingCache<String, List<DictDataRespDTO>> DICT_DATA_CACHE =
            CacheUtils.buildAsyncReloadingCache(
                    Duration.ofMinutes(1L),
                    new CacheLoader<>() {
                        @Override
                        public List<DictDataRespDTO> load(String dictType) {
                            return dictDataApi.getDictDataList(dictType);
                        }
                    });

    /**
     * Called by auto-configuration to inject the API implementation.
     */
    public static void init(DictDataCommonApi api) {
        DictFrameworkUtils.dictDataApi = api;
        log.info("[init] DictFrameworkUtils initialized");
    }

    public static void clearCache() {
        DICT_DATA_CACHE.invalidateAll();
    }

    // ========== Label / Value conversion ==========

    /**
     * Get the display label for a dict value.
     *
     * @param dictType dict type code
     * @param value    the stored value (as Integer)
     * @return label string, or null if not found
     */
    @SneakyThrows
    public static String parseDictDataLabel(String dictType, Integer value) {
        if (value == null) {
            return null;
        }
        return parseDictDataLabel(dictType, String.valueOf(value));
    }

    /**
     * Get the display label for a dict value.
     *
     * @param dictType dict type code
     * @param value    the stored value (as String)
     * @return label string, or null if not found
     */
    @SneakyThrows
    public static String parseDictDataLabel(String dictType, String value) {
        List<DictDataRespDTO> dictDatas = DICT_DATA_CACHE.get(dictType);
        return dictDatas.stream()
                .filter(data -> Objects.equals(data.getValue(), value))
                .map(DictDataRespDTO::getLabel)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the stored value for a display label (reverse lookup, used for Excel import).
     *
     * @param dictType dict type code
     * @param label    the display label
     * @return value string, or null if not found
     */
    @SneakyThrows
    public static String parseDictDataValue(String dictType, String label) {
        List<DictDataRespDTO> dictDatas = DICT_DATA_CACHE.get(dictType);
        return dictDatas.stream()
                .filter(data -> Objects.equals(data.getLabel(), label))
                .map(DictDataRespDTO::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all labels for a dict type (used for Excel dropdown options).
     */
    @SneakyThrows
    public static List<String> getDictDataLabelList(String dictType) {
        List<DictDataRespDTO> dictDatas = DICT_DATA_CACHE.get(dictType);
        return dictDatas.stream()
                .map(DictDataRespDTO::getLabel)
                .toList();
    }

    /**
     * Get all values for a dict type (used for @InDict validation).
     */
    @SneakyThrows
    public static List<String> getDictDataValueList(String dictType) {
        List<DictDataRespDTO> dictDatas = DICT_DATA_CACHE.get(dictType);
        return dictDatas.stream()
                .map(DictDataRespDTO::getValue)
                .toList();
    }

}
