/**
 * The infra module mainly provides two capabilities:
 * <ul>
 *  <li>1. Infrastructure operation and management, supporting upper-layer common and core business functions.
 *    For example: scheduled task management, server information, etc.</li>
 *  <Li>2. Development tools to improve development efficiency and quality.
 *    For example: code generators, API documentation, etc.</Li>
 * </ul>
 *
 * <ul>
 *  <li>1. Controller URLs should start with /infra/ to avoid conflicts with other modules. </li>
 *  <li>2. DataObject table names should start with infra_ for easier identification in the database.</li>
 * </ul>
 */
package com.hdl.soar.module.infra;