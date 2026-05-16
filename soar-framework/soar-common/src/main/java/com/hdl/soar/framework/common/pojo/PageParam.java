package com.hdl.soar.framework.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Schema(description="Pagination parameters")
@Data
public class PageParam implements Serializable {

    private static final Integer PAGE_NO = 1;
    private static final Integer PAGE_SIZE = 10;

    /**
     * Number of items per page - no pagination
     *
     * For example, for export APIs, you can set {@link #pageSize} to -1
     * to disable pagination and query all data.
     */
    public static final Integer PAGE_SIZE_NONE = -1;

    @Schema(description = "Page number, starting from 1", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Page number cannot be null")
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNo = PAGE_NO;

    @Schema(description = "Page size, maximum value is 200", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "Page size cannot be null")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 200, message = "Page size must not exceed 200")
    private Integer pageSize = PAGE_SIZE;

}
