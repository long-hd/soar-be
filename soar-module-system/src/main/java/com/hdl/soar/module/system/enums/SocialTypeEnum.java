package com.hdl.soar.module.system.enums;

import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Enum for social platform types
 */
@Getter
@AllArgsConstructor
public enum SocialTypeEnum implements ArrayValuable<Integer> {
    GOOGLE(10, "GOOGLE"),
    MICROSOFT(20, "MICROSOFT"),
    FACEBOOK(30, "FACEBOOK"),
    GITHUB(40, "GITHUB"),
    ZALO(50, "ZALO"),
    TIKTOK(60, "TIKTOK"),
    TELEGRAM(70, "TELEGRAM"),
    TWITTER(80, "TWITTER");

    /**
     * Type
     */
    private final Integer type;

    /**
     * Type identifier
     */
    private final String source;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SocialTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static SocialTypeEnum valueOfType(Integer type) {
        return ArrayUtil.firstMatch(o -> o.getType().equals(type), values());
    }

}
