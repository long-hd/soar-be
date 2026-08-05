package com.hdl.soar.module.pay.enums;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Payment channel codes.
 * <p>
 * The code is the stable identifier persisted on {@code pay_channel.code} and referenced by orders.
 * The concrete {@code PayClient} implementation bound to each code is introduced in a later slice;
 * here the enum only enumerates the allowed codes and their display names.
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {

    VNPAY("vnpay", "VNPay"),
    MOMO("momo", "MoMo"),
    ZALOPAY("zalopay", "ZaloPay"),
    STRIPE("stripe", "Stripe"),
    PAYPAL("paypal", "PayPal"),
    MOCK("mock", "Mock channel");

    /** Stable channel code, stored on {@code pay_channel.code}. */
    private final String code;
    /** Human-readable channel name. */
    private final String name;

    /**
     * Resolve an enum by its code.
     *
     * @param code channel code
     * @return the matching enum, or {@code null} if the code is unknown
     */
    public static PayChannelEnum of(String code) {
        return ArrayUtil.firstMatch(e -> e.getCode().equals(code), values());
    }

    /**
     * @param code channel code
     * @return whether the code corresponds to a known channel
     */
    public static boolean exists(String code) {
        return of(code) != null;
    }

    /** All known channel codes. */
    public static final String[] CODES = Arrays.stream(values())
            .map(PayChannelEnum::getCode).toArray(String[]::new);

}
