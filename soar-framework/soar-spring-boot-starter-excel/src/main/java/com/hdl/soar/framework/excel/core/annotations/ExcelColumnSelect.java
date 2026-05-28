package com.hdl.soar.framework.excel.core.annotations;

import java.lang.annotation.*;

/**
 * Adds a dropdown select list to an Excel column.
 *
 * <p>Supports two data sources (use one):
 * <ul>
 *   <li>{@link #dictType()} — populates dropdown from dictionary data</li>
 *   <li>{@link #functionName()} — populates dropdown from a Spring bean implementing
 *       {@link com.hdl.soar.framework.excel.core.function.ExcelColumnSelectFunction}</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * @ExcelColumnSelect(dictType = "sys_common_status")
 * @ExcelProperty("Status")
 * private String status;
 * }
 * </pre>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelColumnSelect {

    /**
     * @return dict type code for dropdown values (e.g. "sys_common_status")
     */
    String dictType() default "";

    /**
     * @return bean function name for custom dropdown values
     */
    String functionName() default "";

}
