package com.hdl.soar.framework.common.enums;

import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Time interval enum.
 */
@Getter
@AllArgsConstructor
public enum DateIntervalEnum implements ArrayValuable<Integer> {

    HOUR(0, "Hour"), // // Special: this enum is temporarily not included in the dictionary because it is rarely used
    DAY(1, "Day"),
    WEEK(2, "Week"),
    MONTH(3, "Month"),
    QUARTER(4, "Quarter"),
    YEAR(5, "Year")
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(DateIntervalEnum::getInterval).toArray(Integer[]::new);

    /**
     * Interval value
     */
    private final Integer interval;
    /**
     * Interval name
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static DateIntervalEnum valueOf(Integer interval) {
        return ArrayUtil.firstMatch(item -> item.getInterval().equals(interval), DateIntervalEnum.values());
    }

}