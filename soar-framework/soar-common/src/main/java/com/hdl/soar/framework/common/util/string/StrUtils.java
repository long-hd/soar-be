package com.hdl.soar.framework.common.util.string;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.aspectj.lang.JoinPoint;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * String utility class.
 */
public class StrUtils {

    public static String maxLength(CharSequence str, int maxLength) {
        return StrUtil.maxLength(str, maxLength - 3); // The reason for "-3" is that this method appends "..." exactly
    }

    /**
     * Checks whether the given string starts with any of the provided prefixes.
     * Returns false if either the string or the collection is empty.
     *
     * @param str the input string
     * @param prefixes the prefixes to check against
     * @since 3.0.6
     */
    public static boolean startWithAny(String str, Collection<String> prefixes) {
        if (StrUtil.isEmpty(str) || ArrayUtil.isEmpty(prefixes)) {
            return false;
        }

        for (CharSequence prefix : prefixes) {
            if (StrUtil.startWith(str, prefix, false)) {
                return true;
            }
        }
        return false;
    }

    public static List<Long> splitToLong(String value, CharSequence separator) {
        long[] longs = StrUtil.splitToLong(value, separator);
        return Arrays.stream(longs).boxed().collect(Collectors.toList());
    }

    public static Set<Long> splitToLongSet(String value, CharSequence separator) {
        long[] longs = StrUtil.splitToLong(value, separator);
        return Arrays.stream(longs).boxed().collect(Collectors.toSet());
    }

    public static Set<Long> splitToLongSet(String value) {
        return splitToLongSet(value, StrPool.COMMA);
    }

    public static List<Integer> splitToInteger(String value, CharSequence separator) {
        int[] integers = StrUtil.splitToInt(value, separator);
        return Arrays.stream(integers).boxed().collect(Collectors.toList());
    }

    /**
     * Removes lines from a string that contain the specified sequence.
     *
     * @param content the input string
     * @param sequence the sequence to match within lines
     * @return the resulting string after removal
     */
    public static String removeLineContains(String content, String sequence) {
        if (StrUtil.isEmpty(content) || StrUtil.isEmpty(sequence)) {
            return content;
        }
        return Arrays.stream(content.split("\n"))
                .filter(line -> !line.contains(sequence))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Joins method arguments into a single string.
     *
     * Special case: excludes non-serializable parameters such as
     * ServletRequest, ServletResponse, MultipartFile, etc.
     *
     * @param joinPoint the join point
     * @return concatenated arguments
     */
    public static String joinMethodArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (ArrayUtil.isEmpty(args)) {
            return "";
        }
        return ArrayUtil.join(args, ",", item -> {
            if (item == null) {
                return "";
            }
            // Discussion references: https://t.zsxq.com/XUJVk, https://t.zsxq.com/MnKcL
            String clazzName = item.getClass().getName();
            if (StrUtil.startWithAny(clazzName, "javax.servlet", "jakarta.servlet", "org.springframework.web")) {
                return "";
            }
            return item;
        });
    }

}
