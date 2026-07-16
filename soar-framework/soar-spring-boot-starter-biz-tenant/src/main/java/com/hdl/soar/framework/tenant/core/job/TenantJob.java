package com.hdl.soar.framework.tenant.core.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a scheduled job as multi-tenant: the annotated method is executed once PER TENANT,
 * each execution running under that tenant's context.
 *
 * <p>Background jobs have no logged-in user and no HTTP request, so nothing sets the tenant
 * context for them. Without this, tenant-filtered queries inside the job would see no
 * (or the wrong) tenant. This annotation fills that gap: write the job logic once, and it
 * runs for every tenant with the context correctly set.
 *
 * <p>Must be placed on the job's execute method (see the JobHandler contract).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantJob {
}
