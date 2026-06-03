package com.hdl.soar.framework.datapermission.core.aop;

import com.hdl.soar.framework.datapermission.core.annotation.DataPermission;
import lombok.Getter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor for the {@link DataPermission} annotation.
 *
 * <ol>
 *   <li>Before the method runs: push the resolved {@link DataPermission} onto the context.</li>
 *   <li>After the method returns: pop it.</li>
 * </ol>
 */
@DataPermission // the default-valued annotation here is reused as the DATA_PERMISSION_NULL sentinel
public class DataPermissionAnnotationInterceptor implements MethodInterceptor {

    /**
     * Sentinel for "no annotation found", to distinguish from "not yet looked up" in the cache.
     */
    static final DataPermission DATA_PERMISSION_NULL =
            DataPermissionAnnotationInterceptor.class.getAnnotation(DataPermission.class);

    @Getter
    private final Map<MethodClassKey, DataPermission> dataPermissionCache = new ConcurrentHashMap<>();

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        // push
        DataPermission dataPermission = this.findAnnotation(methodInvocation);
        if (dataPermission != null) {
            DataPermissionContextHolder.add(dataPermission);
        }
        try {
            return methodInvocation.proceed();
        } finally {
            // pop
            if (dataPermission != null) {
                DataPermissionContextHolder.remove();
            }
        }
    }

    private DataPermission findAnnotation(MethodInvocation methodInvocation) {
        // 1. from cache
        Method method = methodInvocation.getMethod();
        Object targetObject = methodInvocation.getThis();
        Class<?> clazz = targetObject != null ? targetObject.getClass() : method.getDeclaringClass();
        MethodClassKey methodClassKey = new MethodClassKey(method, clazz);
        DataPermission dataPermission = dataPermissionCache.get(methodClassKey);
        if (dataPermission != null) {
            return dataPermission != DATA_PERMISSION_NULL ? dataPermission : null;
        }

        // 2.1 from method
        dataPermission = AnnotationUtils.findAnnotation(method, DataPermission.class);
        // 2.2 from class
        if (dataPermission == null) {
            dataPermission = AnnotationUtils.findAnnotation(clazz, DataPermission.class);
        }
        // 2.3 cache (null → sentinel)
        dataPermissionCache.put(methodClassKey, dataPermission != null ? dataPermission : DATA_PERMISSION_NULL);
        return dataPermission;
    }

}
