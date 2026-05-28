package com.hdl.soar.framework.excel.core.convert;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import com.hdl.soar.framework.common.util.json.JsonUtils;

/**
 * FastExcel converter that serializes an object field to a JSON string on export.
 *
 * <p>Usage:
 * <pre>{@code
 * @ExcelProperty(value = "Config", converter = JsonConvert.class)
 * private Map<String, Object> config;
 * }
 * </pre>
 */
public class JsonConvert implements Converter<Object> {

    @Override
    public Class<?> supportJavaTypeKey() {
        throw new UnsupportedOperationException("Not needed");
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        throw new UnsupportedOperationException("Not needed");
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(JsonUtils.toJsonString(value));
    }

}
