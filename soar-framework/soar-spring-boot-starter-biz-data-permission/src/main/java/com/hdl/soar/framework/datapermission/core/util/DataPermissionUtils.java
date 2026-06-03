package com.hdl.soar.framework.datapermission.core.util;

import com.hdl.soar.framework.datapermission.core.annotation.DataPermission;
import com.hdl.soar.framework.datapermission.core.aop.DataPermissionContextHolder;
import lombok.SneakyThrows;

import java.util.concurrent.Callable;

/**
 * Data permission utilities.
 *
 * <p>Use {@link #executeIgnore(Runnable)} / {@link #executeIgnore(Callable)} to run a
 * block of code with data permission disabled, without needing the annotation.
 */
public class DataPermissionUtils {

    private static DataPermission DATA_PERMISSION_DISABLE;

    @DataPermission(enable = false)
    @SneakyThrows
    private static DataPermission getDisableDataPermissionDisable() {
        if (DATA_PERMISSION_DISABLE == null) {
            DATA_PERMISSION_DISABLE = DataPermissionUtils.class
                    .getDeclaredMethod("getDisableDataPermissionDisable")
                    .getAnnotation(DataPermission.class);
        }
        return DATA_PERMISSION_DISABLE;
    }

    /**
     * Run the given logic with data permission ignored.
     */
    public static void executeIgnore(Runnable runnable) {
        addDisableDataPermission();
        try {
            runnable.run();
        } finally {
            removeDataPermission();
        }
    }

    /**
     * Run the given logic with data permission ignored, returning its result.
     */
    @SneakyThrows
    public static <T> T executeIgnore(Callable<T> callable) {
        addDisableDataPermission();
        try {
            return callable.call();
        } finally {
            removeDataPermission();
        }
    }

    public static void addDisableDataPermission() {
        DataPermissionContextHolder.add(getDisableDataPermissionDisable());
    }

    public static void removeDataPermission() {
        DataPermissionContextHolder.remove();
    }

}
