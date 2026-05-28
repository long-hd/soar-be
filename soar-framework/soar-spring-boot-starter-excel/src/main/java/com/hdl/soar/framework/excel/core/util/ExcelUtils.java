package com.hdl.soar.framework.excel.core.util;

import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.converters.longconverter.LongStringConverter;
import com.hdl.soar.framework.excel.core.handler.ColumnWidthMatchStyleStrategy;
import com.hdl.soar.framework.excel.core.handler.SelectSheetWriteHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel utility class wrapping FastExcel for export and import.
 *
 * <p>Export usage in Controller:
 * <pre>{@code
 *  ExcelUtils.write(response, "dict-type.xlsx", "data", DictTypeRespDTO.class, list);
 * }
 * </pre>
 *
 * <p>Import usage:
 * <pre>{@code
 *  List<UserImportDTO> users = ExcelUtils.read(file, UserImportDTO.class);
 * }
 * </pre>
 */
public class ExcelUtils {

    /**
     * Export a list as an Excel file response.
     *
     * @param response  HTTP response (output stream will be written to)
     * @param filename  download filename (e.g. "dict-type.xlsx")
     * @param sheetName Excel sheet name
     * @param head      class annotated with @ExcelProperty (defines columns)
     * @param data      data list
     */
    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data) throws IOException {
        // Write Excel to response output stream
        FastExcelFactory.write(response.getOutputStream(), head)
                .autoCloseStream(false) // Let Servlet manage stream lifecycle
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy())
                .registerWriteHandler(new SelectSheetWriteHandler(head))
                .registerConverter(new LongStringConverter()) // Prevent Long precision loss in Excel
                .sheet(sheetName)
                .doWrite(data);
        // Set headers AFTER writing — if writing fails, response won't have wrong content-type
        response.addHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
    }

    /**
     * Read an uploaded Excel file into a list of objects.
     *
     * @param file uploaded file
     * @param head class annotated with @ExcelProperty
     * @return list of parsed objects
     */
    public static <T> List<T> read(MultipartFile file, Class<T> head) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return FastExcelFactory.read(inputStream, head, null)
                    .autoCloseStream(false)
                    .doReadAllSync();
        }
    }

}
