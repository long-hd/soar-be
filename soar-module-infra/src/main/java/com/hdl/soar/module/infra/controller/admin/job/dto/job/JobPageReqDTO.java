package com.hdl.soar.module.infra.controller.admin.job.dto.job;

import com.hdl.soar.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Admin Backend - Scheduled Job Page Request DTO")
public class JobPageReqDTO extends PageParam {

    @Schema(description = "Job name (fuzzy match)", example = "clean")
    private String name;

    @Schema(description = "Status: 0=INIT, 1=NORMAL, 2=STOP", example = "1")
    private Integer status;

    @Schema(description = "Handler bean name (fuzzy match)", example = "clean")
    private String handlerName;

}