package com.hdl.soar.framework.common.util.collection;

import cn.hutool.core.collection.CollUtil;

import java.util.Set;

/**
 * Set utility class
 */
public class SetUtils {

    @SafeVarargs
    public static <T> Set<T> asSet(T... objs) {return CollUtil.newHashSet(objs);}
}
