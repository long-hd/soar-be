package com.hdl.soar.framework.datapermission.core.aop;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.hdl.soar.framework.datapermission.core.annotation.DataPermission;

import java.util.LinkedList;
import java.util.List;

/**
 * Thread-local context holding the active {@link DataPermission} annotations.
 *
 * <p>A {@link LinkedList} is used as a stack because method calls can nest
 * (an annotated method calling another annotated method). Uses
 * {@link TransmittableThreadLocal} so the context propagates to async/pooled
 * threads, consistent with the tenant module.
 */
public class DataPermissionContextHolder {

    private static final ThreadLocal<LinkedList<DataPermission>> DATA_PERMISSIONS =
            TransmittableThreadLocal.withInitial(LinkedList::new);

    /**
     * @return the current (innermost) {@link DataPermission}, or {@code null} if none
     */
    public static DataPermission get() {
        return DATA_PERMISSIONS.get().peekLast();
    }

    /**
     * Push a {@link DataPermission} onto the stack.
     */
    public static void add(DataPermission dataPermission) {
        DATA_PERMISSIONS.get().addLast(dataPermission);
    }

    /**
     * Pop the innermost {@link DataPermission}. Clears the thread-local when empty.
     */
    public static DataPermission remove() {
        DataPermission dataPermission = DATA_PERMISSIONS.get().removeLast();
        if (DATA_PERMISSIONS.get().isEmpty()) {
            DATA_PERMISSIONS.remove();
        }
        return dataPermission;
    }

    /**
     * @return all {@link DataPermission} currently on the stack
     */
    public static List<DataPermission> getAll() {
        return DATA_PERMISSIONS.get();
    }

    /**
     * Clear the context. Intended for unit tests only.
     */
    public static void clear() {
        DATA_PERMISSIONS.remove();
    }

}
