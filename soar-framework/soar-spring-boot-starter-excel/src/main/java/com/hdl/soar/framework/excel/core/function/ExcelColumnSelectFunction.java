package com.hdl.soar.framework.excel.core.function;

import java.util.List;

/**
 * Interface for providing custom dropdown options to Excel columns.
 *
 * <p>Implement this as a Spring bean and reference by name
 * via {@code @ExcelColumnSelect(functionName = "yourBeanName")}.
 *
 * <p>Example:
 * <pre>{@code
 * @Component
 * public class DeptSelectFunction implements ExcelColumnSelectFunction {
 *     public String getName() { return "dept"; }
 *     public List<String> getOptions() { return deptService.getAllNames(); }
 * }
 * }
 * </pre>
 */
public interface ExcelColumnSelectFunction {
    /**
     * @return unique function name, referenced in @ExcelColumnSelect(functionName)
     */
    String getName();

    /**
     * @return list of dropdown option labels
     */
    List<String> getOptions();

}
