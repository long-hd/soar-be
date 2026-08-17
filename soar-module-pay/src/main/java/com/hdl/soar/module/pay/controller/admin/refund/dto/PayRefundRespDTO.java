package com.hdl.soar.module.pay.controller.admin.refund.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PayRefundRespDTO {
    private Long id;
    private String no;
    private Long appId;
    private Long channelId;
    private String channelCode;
    private Long orderId;
    private String orderNo;
    private String merchantOrderId;
    private String merchantRefundId;
    private Integer status;
    private BigDecimal payPrice;
    private BigDecimal refundPrice;
    private String reason;
    private String channelRefundNo;
    private Instant successTime;
    private String channelErrorCode;
    private String channelErrorMsg;
    private Instant createTime;
}