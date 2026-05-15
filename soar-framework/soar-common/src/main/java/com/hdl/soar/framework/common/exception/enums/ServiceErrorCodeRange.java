package com.hdl.soar.framework.common.exception.enums;

/**
 * Business exception error code ranges.
 *
 * Used to solve cross-module error code conflicts by only declaring ranges without actual usage.
 *
 * The code consists of 10 digits, divided into four parts:
 *
 * Part 1 (1 digit): Type
 *   1 - Business-level exception
 *   x - Reserved
 *
 * Part 2 (3 digits): System type
 *   001 - User system
 *   002 - Product system
 *   003 - Order system
 *   004 - Payment system
 *   005 - Coupon system
 *   ... - ...
 *
 * Part 3 (3 digits): Module
 *   No strict rules.
 *   Generally, each system may contain multiple modules, which can be further divided.
 *   Example for user system:
 *     001 - OAuth2 module
 *     002 - User module
 *     003 - MobileCode module
 *
 * Part 4 (3 digits): Error code
 *   No strict rules.
 *   Generally, increment within each module.
 *
 */
public class ServiceErrorCodeRange {
    // infra module error code range [1-001-000-000 ~ 1-002-000-000)
    // system module error code range [1-002-000-000 ~ 1-003-000-000)
    // report module error code range [1-003-000-000 ~ 1-004-000-000)
    // member module error code range [1-004-000-000 ~ 1-005-000-000)
    // mp module error code range [1-006-000-000 ~ 1-007-000-000)
    // pay module error code range [1-007-000-000 ~ 1-008-000-000)
    // bpm module error code range [1-009-000-000 ~ 1-010-000-000)

    // product module error code range [1-008-000-000 ~ 1-009-000-000)
    // trade module error code range [1-011-000-000 ~ 1-012-000-000)
    // promotion module error code range [1-013-000-000 ~ 1-014-000-000)

    // crm module error code range [1-020-000-000 ~ 1-021-000-000)

    // ai module error code range [1-022-000-000 ~ 1-023-000-000)
}
