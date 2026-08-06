package com.hdl.soar.module.pay.controller.admin.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Admin backend - Notify task Response DTO")
public class PayNotifyTaskRespDTO {

    private Long id;
    private Long appId;
    private Integer type;
    private Long dataId;
    private String merchantOrderId;
    private String notifyUrl;
    private Integer status;
    private Instant nextNotifyTime;
    private Instant lastExecuteTime;
    private Integer notifyTimes;
    private Integer maxNotifyTimes;
    private Instant createTime;

}
