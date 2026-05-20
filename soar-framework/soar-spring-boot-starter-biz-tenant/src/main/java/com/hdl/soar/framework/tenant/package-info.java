/**
 * Multi-tenancy support includes the following aspects:
 *
 * <ul>
 *   <li>
 *     <b>DB:</b>
 *     Implemented based on Jpa multi-tenant functionality.
 *   </li>
 *
 *   <li>
 *     <b>Redis:</b>
 *     Isolation is achieved by appending the tenant ID to Redis keys.
 *   </li>
 *
 *   <li>
 *     <b>Web:</b>
 *     Parses the <code>tenant-id</code> header from HTTP requests
 *     and adds it to the tenant context.
 *   </li>
 *
 *   <li>
 *     <b>Security:</b>
 *     Verifies whether the current user is accessing data
 *     from other tenants without authorization.
 *   </li>
 *
 *   <li>
 *     <b>Job:</b>
 *     Executes tasks independently and in parallel for each tenant.
 *   </li>
 *
 *   <li>
 *     <b>MQ:</b>
 *     Producer attaches the tenant ID to message headers;
 *     Consumer extracts the tenant ID and adds it to the tenant context.
 *   </li>
 *
 *   <li>
 *     <b>Async:</b>
 *     Ensures ThreadLocal propagation using
 *     <code>TransmittableThreadLocal</code>.
 *
 *     <p>Related integration points:</p>
 *
 *     <ul>
 *       <li>
 *         Spring Async:
 *         {@link com.hdl.soar.framework.quartz.config.SoarAsyncAutoConfiguration#threadPoolTaskExecutorBeanPostProcessor()}
 *       </li>
 *
 *       <li>
 *         Spring Security:
 *         <code>TransmittableThreadLocalSecurityContextHolderStrategy</code>,
 *         <code>SoarSecurityAutoConfiguration#securityContextHolderMethodInvokingFactoryBean()</code>
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 */
package com.hdl.soar.framework.tenant;