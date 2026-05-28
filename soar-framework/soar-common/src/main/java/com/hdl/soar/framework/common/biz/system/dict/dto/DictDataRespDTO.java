package com.hdl.soar.framework.common.biz.system.dict.dto;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import lombok.Data;

/**
 * Dict data Response DTO — lightweight projection for cross-module dict lookups.
 */
@Data
public class DictDataRespDTO {

    /**
     * Display label (e.g. "Enabled", "Disabled")
     */
    private String label;

    /**
     * Stored value (e.g. "0", "1")
     */
    private String value;

    /**
     * Dict type code (e.g. "sys_common_status")
     */
    private String dictType;

    /**
     * Status — see {@link CommonStatusEnum}
     */
    private Integer status;

}
