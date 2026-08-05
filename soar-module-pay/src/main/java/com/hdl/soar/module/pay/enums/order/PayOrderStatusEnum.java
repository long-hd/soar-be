package com.hdl.soar.module.pay.enums.order;

import com.hdl.soar.framework.common.core.ArrayValuable;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Payment order (and order-extension) status.
 * <p>
 * Both {@code pay_order} and {@code pay_order_extension} use this state set. The state machine only
 * allows the transitions {@code WAITING -> SUCCESS} and {@code WAITING -> CLOSED}; {@code REFUND} is
 * driven by the refund flow (a later slice) and never returns to {@code SUCCESS}.
 */
@Getter
@AllArgsConstructor
public enum PayOrderStatusEnum implements ArrayValuable<Integer> {

    WAITING(0, "Waiting for payment"),
    SUCCESS(10, "Paid"),
    REFUND(20, "Refunded"),
    CLOSED(30, "Closed");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PayOrderStatusEnum::getStatus).toArray(Integer[]::new);

    /** JPA converter: maps this enum to its integer status column. */
    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<PayOrderStatusEnum> {
        protected JpaConverter() {
            super(PayOrderStatusEnum.class, PayOrderStatusEnum::getStatus);
        }
    }

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PayOrderStatusEnum of(Integer status) {
        if (status == null) {
            return null;
        }
        for (PayOrderStatusEnum e : values()) {
            if (e.getStatus().equals(status)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isWaiting(Integer status) {
        return Objects.equals(status, WAITING.getStatus());
    }

    public static boolean isSuccess(Integer status) {
        return Objects.equals(status, SUCCESS.getStatus());
    }

    public static boolean isClosed(Integer status) {
        return Objects.equals(status, CLOSED.getStatus());
    }

    public static boolean isRefund(Integer status) {
        return Objects.equals(status, REFUND.getStatus());
    }

}
