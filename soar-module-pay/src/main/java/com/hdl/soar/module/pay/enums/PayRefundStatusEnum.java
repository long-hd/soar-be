package com.hdl.soar.module.pay.enums;

import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.core.ArrayValuable;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Refund status.
 * <p>
 * Distinct from {@code PayOrderStatusEnum}: a refund has a real {@link #FAILURE} terminal state
 * because a channel can reject a money-out request. The state machine allows only
 * {@code WAITING -> SUCCESS} and {@code WAITING -> FAILURE}; both are terminal.
 */
@Getter
@AllArgsConstructor
public enum PayRefundStatusEnum implements ArrayValuable<Integer> {

    WAITING(0, "Refunding"),
    SUCCESS(10, "Refunded"),
    FAILURE(20, "Refund failed");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PayRefundStatusEnum::getStatus).toArray(Integer[]::new);

    /** JPA converter: maps this enum to its integer status column. */
    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<PayRefundStatusEnum> {
        protected JpaConverter() {
            super(PayRefundStatusEnum.class, PayRefundStatusEnum::getStatus);
        }
    }

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PayRefundStatusEnum of(Integer status) {
        return ArrayUtil.firstMatch(e -> e.getStatus().equals(status), PayRefundStatusEnum.values());
    }

    public static boolean isSuccess(Integer status) {
        return Objects.equals(status, SUCCESS.getStatus());
    }

    public static boolean isFailure(Integer status) {
        return Objects.equals(status, FAILURE.getStatus());
    }

}
