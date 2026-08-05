package com.hdl.soar.module.pay.dal.entity.order;


import com.hdl.soar.framework.jpa.core.converter.JsonStringMapConverter;
import com.hdl.soar.framework.jpa.core.entity.BasePO;
import com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.Map;

/**
 * Payment order extension: one attempt to pay an order through one channel.
 * <p>
 * A new row is created every time an order is submitted to a channel. Its {@link #no} is the external
 * order number handed to the rail (unique per attempt — rails reject duplicates), and channel callbacks
 * are matched back to this row by {@link #no}. An order may have many CLOSED extensions and at most one
 * SUCCESS.
 * <p>
 * Global (extends {@link BasePO}).
 */
@Entity
@Table(name = "pay_order_extension")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class PayOrderExtensionPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** External order number handed to the rail; unique per attempt. */
    @Column(name = "no", nullable = false)
    private String no;

    /** Owning order id. References {@link PayOrderPO#getId()}. */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** Channel id used for this attempt. */
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    /** Channel code used for this attempt. */
    @Column(name = "channel_code", nullable = false)
    private String channelCode;

    /** Client IP. */
    @Column(name = "user_ip")
    private String userIp;

    /**
     * Status.
     * <p>
     * Enum {@link PayOrderStatusEnum}
     */
    @Column(name = "status", nullable = false)
    private PayOrderStatusEnum status;

    /** Extra channel parameters. Stored as JSON. */
    @Convert(converter = JsonStringMapConverter.class)
    @Column(name = "channel_extras")
    private Map<String, String> channelExtras;

    /** Channel error code, when the attempt failed. */
    @Column(name = "channel_error_code")
    private String channelErrorCode;

    /** Channel error message, when the attempt failed. */
    @Column(name = "channel_error_msg")
    private String channelErrorMsg;

    /** Raw channel notify/return payload. */
    @Column(name = "channel_notify_data")
    private String channelNotifyData;

}
