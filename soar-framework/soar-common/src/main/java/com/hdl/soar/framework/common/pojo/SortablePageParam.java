package com.hdl.soar.framework.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "Pageable parameters with sorting support")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SortablePageParam extends PageParam {

    @Schema(description = "Sortable field")
    private List<SortingField> sortingFields;

}