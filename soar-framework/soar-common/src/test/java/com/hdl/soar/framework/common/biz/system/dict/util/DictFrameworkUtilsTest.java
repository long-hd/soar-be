package com.hdl.soar.framework.common.biz.system.dict.util;

import com.hdl.soar.framework.common.biz.system.dict.DictDataCommonApi;
import com.hdl.soar.framework.common.biz.system.dict.dto.DictDataRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the static dict lookup util. The dependency (DictDataCommonApi) is a mock,
 * injected through the util's own init(...) entry point - no Spring context needed.
 */
public class DictFrameworkUtilsTest {

    private static final String DICT_TYPE = "common_status";

    private DictDataCommonApi dictDataApi;

    @BeforeEach
    void setUp() {
        dictDataApi = mock(DictDataCommonApi.class);
        DictFrameworkUtils.init(dictDataApi);   // inject the mock
        DictFrameworkUtils.clearCache();        // static cache is shared across tests -> reset
    }

    @Test
    @DisplayName("parseDictDataLabel returns the matching label for a value")
    void parseDictDataLabel_returnsLabel() {
        when(dictDataApi.getDictDataList(DICT_TYPE))
                .thenReturn(List.of(dict("Enabled", "0"), dict("Disabled", "1")));

        assertThat(DictFrameworkUtils.parseDictDataLabel(DICT_TYPE, "1")).isEqualTo("Disabled");
        assertThat(DictFrameworkUtils.parseDictDataLabel(DICT_TYPE, 0)).isEqualTo("Enabled");
        assertThat(DictFrameworkUtils.parseDictDataLabel(DICT_TYPE, "99")).isNull(); // no match
    }

    @Test
    @DisplayName("reverse and list lookups work")
    void reverseAndListLookups() {
        when(dictDataApi.getDictDataList(DICT_TYPE))
                .thenReturn(List.of(dict("Enabled", "0"), dict("Disabled", "1")));

        assertThat(DictFrameworkUtils.parseDictDataValue(DICT_TYPE, "Disabled")).isEqualTo("1");
        assertThat(DictFrameworkUtils.getDictDataLabelList(DICT_TYPE))
                .containsExactly("Enabled", "Disabled");
        assertThat(DictFrameworkUtils.getDictDataValueList(DICT_TYPE))
                .containsExactly("0", "1");
    }

    @Test
    @DisplayName("unknown dict type does not NPE (Caffeine get may return null)")
    void unknownDictType_returnsEmptyNotNpe() {
        // Loader returns null for an unknown type. Caffeine's get() then returns null;
        // the getCachedDictDataList() null-coalesce must turn that into an empty list.
        when(dictDataApi.getDictDataList("unknown")).thenReturn(null);

        assertThat(DictFrameworkUtils.parseDictDataLabel("unknown", "0")).isNull();
        assertThat(DictFrameworkUtils.getDictDataLabelList("unknown")).isEmpty();
        assertThat(DictFrameworkUtils.getDictDataValueList("unknown")).isEmpty();
    }

    /** Small factory for a dict entry. */
    private static DictDataRespDTO dict(String label, String value) {
        DictDataRespDTO dto = new DictDataRespDTO();
        dto.setLabel(label);
        dto.setValue(value);
        return dto;
    }

}
