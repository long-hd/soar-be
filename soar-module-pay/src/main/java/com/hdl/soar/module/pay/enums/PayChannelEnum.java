package com.hdl.soar.module.pay.enums;

import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClient;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClientConfig;
import com.hdl.soar.module.pay.framework.pay.core.client.impl.NonePayClientConfig;
import com.hdl.soar.module.pay.framework.pay.core.client.impl.mock.MockPayClient;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

import static com.hdl.soar.module.pay.enums.PayCurrencyEnum.VND;

/**
 * Payment channel (rail) descriptor: everything known about a rail in one place — its code, the
 * currencies it accepts, its config type, and its client type.
 * <p>
 * Only rails with a client implementation are listed. A rail is added here when its client is built.
 * The config/client classes are resolved by {@code code}, so stored channel config never contains a
 * Java class name.
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {

    MOCK("mock", Set.of(VND), NonePayClientConfig.class, MockPayClient.class);
    // VNPAY("vnpay", Set.of(VND), VnpayPayClientConfig.class, VnpayPayClient.class) — added in 2b

    /** Stable rail identifier, stored in {@code pay_channel.code}. */
    private final String code;

    /** Currencies this rail accepts. */
    private final Set<PayCurrencyEnum> supportedCurrencies;

    /** Config type used to parse this rail's stored JSON. */
    private final Class<? extends PayClientConfig> configClass;

    /** Client type instantiated for this rail. */
    private final Class<? extends PayClient> clientClass;

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
