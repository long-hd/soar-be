package com.hdl.soar.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Sorting field DTO
 *
 * The class name includes "ing" to avoid naming conflicts with ES SortField.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortingField implements Serializable {

    /**
     * Order - ascending
     */
    public static final String ORDER_ASC = "asc";
    /**
     * Order - descending
     */
    public static final String ORDER_DESC = "desc";

    /**
     * Sort Field
     */
    private String field;
    /**
     * Sort Order
     */
    private String order;

}
