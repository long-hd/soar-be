package com.hdl.soar.framework.common.util.collection;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.framework.common.pojo.PageResult;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Collection Util class
 */
public class CollectionUtils {

    // =============================== Collection Util
    public static boolean containsAny(Object source, Object... targets) {
        return asList(targets).contains(source);
    }

    public static boolean isAnyEmpty(Collection<?>... collections) {
        return Arrays.stream(collections).anyMatch(CollectionUtil::isEmpty);
    }

    public static <T> boolean anyMatch(Collection<T> from, Predicate<T> predicate) {
        return from.stream().anyMatch(predicate);
    }

    // =============================== List Util
    public static <T> List<T> filterList(Collection<T> from, Predicate<T> predicate) {
        if (CollUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return from.stream().filter(predicate).collect(Collectors.toList());
    }

    public static <T, U> List<U> convertList(T[] from, Function<T, U> func) {
        if (ArrayUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return convertList(Arrays.asList(from), func);
    }

    public static <T, U> List<U> convertList(Collection<T> from, Function<T, U> func) {
        if (CollUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return from.stream().map(func).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static <T, U> List<U> convertList(Collection<T> from, Function<T, U> func, Predicate<T> filter) {
        if (CollUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return from.stream().filter(filter).map(func).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static <T, U> PageResult<U> convertPage(PageResult<T> from, Function<T, U> func) {
        if (ArrayUtil.isEmpty(from)) {
            return new PageResult<>(from.getTotal());
        }
        return new PageResult<>(convertList(from.getList(), func), from.getTotal());
    }

    // =============================== Set Util
    public static <T, U> Set<U> convertSet(Collection<T> from, Function<T, U> func) {
        if (CollUtil.isEmpty(from)) {
            return new HashSet<>();
        }
        return from.stream().map(func).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static <T> Set<T> convertSet(Collection<T> from) {
        return convertSet(from, v -> v);
    }

    public static <T, U> Set<U> convertSet(Collection<T> from, Function<T, U> func, Predicate<T> filter) {
        if (CollUtil.isEmpty(from)) {
            return new HashSet<>();
        }
        return from.stream().filter(filter).map(func).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    // =============================== Map Util

}
