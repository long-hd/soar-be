package com.hdl.soar.framework.excel.core.convert;

import cn.hutool.core.convert.Convert;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import com.hdl.soar.framework.common.biz.system.dict.util.DictFrameworkUtils;
import com.hdl.soar.framework.excel.core.annotations.DictFormat;
import lombok.extern.slf4j.Slf4j;

/**
 * FastExcel converter that translates dict values to/from display labels.
 *
 * <p>On <b>export</b> (write): converts the field value (Integer/String) to its dict label.
 * <p>On <b>import</b> (read): converts the cell label text back to the field value.
 *
 * <p>Usage: pair with {@link DictFormat} annotation on the DTO field:
 * <pre>{@code
 * @ExcelProperty(value = "Status", converter = DictConvert.class)
 * @DictFormat("sys_common_status")
 * private Integer status;
 * }
 * </pre>
 */
@Slf4j
public class DictConvert implements Converter<Object> {
    @Override
    public Class<?> supportJavaTypeKey() {
        throw new UnsupportedOperationException("Not needed — type is inferred from field");
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        throw new UnsupportedOperationException("Not needed — type is inferred from field");
    }

    @Override
    public Object convertToJavaData(ReadCellData<?> readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        String type = getType(contentProperty);
        String label = readCellData.getStringValue();
        String value = DictFrameworkUtils.parseDictDataValue(type, label);
        if (value == null) {
            log.error("[convertToJavaData] dictType({}) cannot parse label({})", type, label);
            return null;
        }
        // Convert String value to the actual field type (Integer, String, etc.)
        Class<?> fieldClazz = contentProperty.getField().getType();
        return Convert.convert(fieldClazz, value);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (object == null) {
            return new WriteCellData<>("");
        }
        String type = getType(contentProperty);
        String value = String.valueOf(object);
        String label = DictFrameworkUtils.parseDictDataLabel(type, value);
        if (label == null) {
            log.error("[convertToExcelData] dictType({}) cannot find label for value({})", type, value);
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(label);
    }

    private static String getType(ExcelContentProperty contentProperty) {
        return contentProperty.getField().getAnnotation(DictFormat.class).value();
    }

}
