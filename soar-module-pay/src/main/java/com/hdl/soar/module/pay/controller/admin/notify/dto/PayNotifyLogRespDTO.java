package com.hdl.soar.module.pay.controller.admin.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin backend - Notify log Response DTO")
public class PayNotifyLogRespDTO {

    private Long id;
    private Long taskId;
    private Integer notifyTimes;
    private Integer status;
    private String response;
    private Instant createTime;

}
