package com.hdl.soar.framework.excel.core.annotations;

import java.lang.annotation.*;

/**
 * Marks an Excel field for dictionary value <-> label conversion.
 *
 * <p>Used together with {@link com.hdl.soar.framework.excel.core.convert.DictConvert}:
 * <pre>{@code
 * @ExcelProperty(value = "Status", converter = DictConvert.class)
 * @DictFormat("sys_common_status")
 * private Integer status;
 * }
 * </pre>
 *
 * <p>On export: Integer value → display label (e.g. 0 → "Enabled")
 * <p>On import: display label → Integer value (e.g. "Enabled" → 0)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DictFormat {

    /**
     * @return dict type code (e.g. "sys_common_status")
     */
    String value();

}