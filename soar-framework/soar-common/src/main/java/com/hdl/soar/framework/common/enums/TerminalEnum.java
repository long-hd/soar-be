package com.hdl.soar.framework.common.enums;

import com.hdl.soar.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Terminal enum.
 */
@Getter
@RequiredArgsConstructor
public enum TerminalEnum implements ArrayValuable<Integer> {

    UNKNOWN(0, "Unknown"), // Used when the terminal cannot be identified
    WECHAT_MINI_PROGRAM(10, "WeChat Mini Program"),
    WECHAT_WAP(11, "WeChat Official Account"),
    H5(20, "H5 Web Page"),
    APP(31, "Mobile App"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(TerminalEnum::getTerminal).toArray(Integer[]::new);

    /**
     * Terminal value
     */
    private final Integer terminal;
    /**
     * Terminal name
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
