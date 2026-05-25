package com.hdl.soar.framework.common.util.object;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Object utility class
 */
public class ObjectUtils {

    /**
     * Clone an object while ignoring the ID field
     *
     * @param object the object to be cloned
     * @param consumer a consumer that allows further modification of the cloned object
     * @return the cloned object
     */
    public static <T> T cloneIgnoreId(T object, Consumer<T> consumer) {
        T result = ObjectUtil.clone(object);
        // ignore the id field
        Field field = ReflectUtil.getField(object.getClass(), "id");
        if (field != null) {
            ReflectUtil.setFieldValue(result, field, null);
        }
        // further modification
        if (result != null) {
            consumer.accept(result);
        }
        return result;
    }

    public static <T extends Comparable<T>> T max(T obj1, T obj2) {
        if (obj1 == null) {
            return obj2;
        }
        if (obj2 == null) {
            return obj1;
        }
        return obj1.compareTo(obj2) > 0 ? obj1 : obj2;
    }

    @SafeVarargs
    public static <T> T defaultIfNull(T... array) {
        for (T item : array) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    @SafeVarargs
    public static <T> boolean equalsAny(T obj, T... array) {
        return Arrays.asList(array).contains(obj);
    }

    @SafeVarargs
    public static <T> boolean notEqualsAny(T obj, T... array) {
        return !Arrays.asList(array).contains(obj);
    }

    public static boolean isNotAllEmpty(Object... objs) {
        return !ObjectUtil.isAllEmpty(objs);
    }

}
