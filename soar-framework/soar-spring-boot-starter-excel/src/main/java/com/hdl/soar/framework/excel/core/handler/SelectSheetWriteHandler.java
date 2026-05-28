package com.hdl.soar.framework.excel.core.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.write.handler.SheetWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteWorkbookHolder;
import com.hdl.soar.framework.common.biz.system.dict.util.DictFrameworkUtils;
import com.hdl.soar.framework.excel.core.annotations.ExcelColumnSelect;
import com.hdl.soar.framework.excel.core.function.ExcelColumnSelectFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Creates a hidden "dict" sheet with dropdown values, then links them
 * to the main sheet columns via data validation.
 *
 * <p>Scans the head class for fields annotated with {@link ExcelColumnSelect},
 * resolves the dropdown values (from dict or from custom function), and
 * creates the appropriate Excel data validation.
 */
@Slf4j
public class SelectSheetWriteHandler implements SheetWriteHandler {

    private static final int FIRST_ROW = 1;     // Data starts at row 1 (row 0 = header)
    private static final int LAST_ROW = 2000;    // Apply dropdown for 2000 rows
    private static final String DICT_SHEET_NAME = "dict_options";

    /**
     * key: column index → value: dropdown options
     */
    private final Map<Integer, List<String>> selectMap = new LinkedHashMap<>();

    public SelectSheetWriteHandler(Class<?> head) {
        int colIndex = 0;
        boolean ignoreUnannotated = head.isAnnotationPresent(ExcelIgnoreUnannotated.class);
        for (Field field : head.getDeclaredFields()) {
            // Skip static final or transient fields (FastExcel ignores these)
            if (isStaticFinalOrTransient(field)) {
                continue;
            }
            // Skip ignored fields
            if ((ignoreUnannotated && !field.isAnnotationPresent(ExcelProperty.class))
                    || field.isAnnotationPresent(ExcelIgnore.class)) {
                continue;
            }

            // Process @ExcelColumnSelect
            if (field.isAnnotationPresent(ExcelColumnSelect.class)) {
                ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
                if (excelProperty != null && excelProperty.index() != -1) {
                    colIndex = excelProperty.index();
                }
                resolveSelectData(colIndex, field);
            }
            colIndex++;
        }
    }

    private boolean isStaticFinalOrTransient(Field field) {
        return (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                || Modifier.isTransient(field.getModifiers());
    }

    private void resolveSelectData(int colIndex, Field field) {
        ExcelColumnSelect columnSelect = field.getAnnotation(ExcelColumnSelect.class);
        String dictType = columnSelect.dictType();
        String functionName = columnSelect.functionName();
        if (ObjectUtil.isEmpty(dictType) && ObjectUtil.isEmpty(functionName)) {
            throw new IllegalArgumentException(
                    "Field(" + field.getName() + ") @ExcelColumnSelect: dictType and functionName cannot both be empty");
        }

        // Option 1: dict type
        if (StrUtil.isNotEmpty(dictType)) {
            selectMap.put(colIndex, DictFrameworkUtils.getDictDataLabelList(dictType));
            return;
        }

        // Option 2: custom function bean
        Map<String, ExcelColumnSelectFunction> functionMap =
                SpringUtil.getApplicationContext().getBeansOfType(ExcelColumnSelectFunction.class);
        ExcelColumnSelectFunction function = functionMap.values().stream()
                .filter(f -> f.getName().equals(functionName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No ExcelColumnSelectFunction found with name: " + functionName));
        selectMap.put(colIndex, function.getOptions());
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (selectMap.isEmpty()) {
            return;
        }

        DataValidationHelper helper = writeSheetHolder.getSheet().getDataValidationHelper();
        Workbook workbook = writeWorkbookHolder.getWorkbook();

        // Sort by list size ascending — required to avoid POI errors with named ranges
        List<Map.Entry<Integer, List<String>>> entries = new ArrayList<>(selectMap.entrySet());
        entries.sort(Comparator.comparingInt(e -> e.getValue().size()));

        // Create hidden dict sheet
        Sheet dictSheet = workbook.createSheet(DICT_SHEET_NAME);
        for (Map.Entry<Integer, List<String>> entry : entries) {
            int col = entry.getKey();
            List<String> options = entry.getValue();

            // Write options to dict sheet (each column = one dropdown list)
            for (int i = 0; i < options.size(); i++) {
                Row row = dictSheet.getRow(i);
                if (row == null) {
                    row = dictSheet.createRow(i);
                }
                row.createCell(col).setCellValue(options.get(i));
            }

            // Create named range and data validation
            setColumnSelect(writeSheetHolder, workbook, helper, col, options.size());
        }
    }

    private static void setColumnSelect(WriteSheetHolder writeSheetHolder, Workbook workbook,
                                        DataValidationHelper helper, int colIndex, int optionCount) {
        // Create named range
        Name name = workbook.createName();
        String excelColumn = CellReference.convertNumToColString(colIndex);
        String refers = DICT_SHEET_NAME + "!$" + excelColumn + "$1:$" + excelColumn + "$" + optionCount;
        name.setNameName("dict" + colIndex);
        name.setRefersToFormula(refers);

        // Create data validation
        DataValidationConstraint constraint = helper.createFormulaListConstraint("dict" + colIndex);
        CellRangeAddressList rangeAddressList = new CellRangeAddressList(FIRST_ROW, LAST_ROW, colIndex, colIndex);
        DataValidation validation = helper.createValidation(constraint, rangeAddressList);
        if (validation instanceof HSSFDataValidation) {
            validation.setSuppressDropDownArrow(false);
        } else {
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
        }
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("Invalid value", "Please select from the dropdown list");
        writeSheetHolder.getSheet().addValidationData(validation);
    }

}
