package com.hdl.soar.module.pay.enums.notify;

import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.core.ArrayValuable;
import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Notify status. Used for both {@code pay_notify_task} and {@code pay_notify_log}, with a subset of
 * the values valid in each:
 * <ul>
 *   <li><b>task</b>: {@link #WAITING} (not yet delivered / retrying), {@link #SUCCESS} (merchant
 *       acked, terminal), {@link #FAILURE} (retries exhausted, terminal).</li>
 *   <li><b>log</b>: one row per attempt, always terminal — {@link #SUCCESS} or {@link #FAILURE}.
 *       {@link #WAITING} never appears in a log row.</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum PayNotifyStatusEnum implements ArrayValuable<Integer> {

    WAITING(0, "Waiting"),
    SUCCESS(10, "Merchant acknowledged"),
    FAILURE(20, "Retries exhausted");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PayNotifyStatusEnum::getStatus).toArray(Integer[]::new);

    /** JPA converter: maps this enum to its integer {@code status} column. */
    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<PayNotifyStatusEnum> {
        protected JpaConverter() {
            super(PayNotifyStatusEnum.class, PayNotifyStatusEnum::getStatus);
        }
    }

    private final Integer status;
    private final String name;

    public static PayNotifyStatusEnum of(Integer status) {
        return ArrayUtil.firstMatch(e -> e.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isSuccess(Integer status) {
        return Objects.equals(status, SUCCESS.getStatus());
    }

}
