/**
 * Data permission starter.
 *
 * <p>Provides row-level data filtering based on the logged-in user's department/self scope
 *
 * <p>Mechanism: Soar hooks into Hibernate's
 * {@link org.hibernate.resource.jdbc.spi.StatementInspector}. The inspector parses each emitted SQL
 * with JSqlParser, matches tables registered by a {@code DataPermissionRule}, and injects a
 * {@code WHERE} expression such as {@code (dept_id IN (...) OR creator = ?)}.
 *
 * <p>Package layout (filled in subsequent blocks):
 * <ul>
 *   <li>{@code core.annotation} — {@code @DataPermission} (enable / include / exclude rules)</li>
 *   <li>{@code core.aop} — context holder + advisor (reuses the TenantIgnore pattern)</li>
 *   <li>{@code core.rule} — {@code DataPermissionRule}, factory, and the dept rule</li>
 *   <li>{@code core.db} — the {@code StatementInspector} implementation</li>
 *   <li>{@code config} — auto-configuration wiring</li>
 * </ul>
 *
 * @author hdl
 */
package com.hdl.soar.framework.datapermission;