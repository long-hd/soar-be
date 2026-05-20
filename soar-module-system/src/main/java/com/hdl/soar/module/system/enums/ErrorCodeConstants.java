package com.hdl.soar.module.system.enums;

import com.hdl.soar.framework.common.exception.ErrorCode;

/**
 * System error code enum class
 *<p></p>
 * System module uses code range 1-002-000-000
 */
public interface ErrorCodeConstants {

    // ========== AUTH module 1-002-000-000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1_002_000_000, "Login failed: incorrect username or password");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1_002_000_001, "Login failed: account is disabled");
    ErrorCode AUTH_LOGIN_CAPTCHA_CODE_ERROR = new ErrorCode(1_002_000_004, "Invalid captcha code, reason: {}");
    ErrorCode AUTH_THIRD_LOGIN_NOT_BIND = new ErrorCode(1_002_000_005, "Account not bound, binding required");
    ErrorCode AUTH_MOBILE_NOT_EXISTS = new ErrorCode(1_002_000_007, "Mobile number does not exist");
    ErrorCode AUTH_REGISTER_CAPTCHA_CODE_ERROR = new ErrorCode(1_002_000_008, "Invalid captcha code, reason: {}");

    // ========== Menu module 1-002-001-000 ==========
    ErrorCode MENU_NAME_DUPLICATE = new ErrorCode(1_002_001_000, "Menu name already exists");
    ErrorCode MENU_PARENT_NOT_EXISTS = new ErrorCode(1_002_001_001, "Parent menu does not exist");
    ErrorCode MENU_PARENT_ERROR = new ErrorCode(1_002_001_002, "Cannot set itself as parent menu");
    ErrorCode MENU_NOT_EXISTS = new ErrorCode(1_002_001_003, "Menu does not exist");
    ErrorCode MENU_EXISTS_CHILDREN = new ErrorCode(1_002_001_004, "Cannot delete menu with child nodes");
    ErrorCode MENU_PARENT_NOT_DIR_OR_MENU = new ErrorCode(1_002_001_005, "Parent menu must be a directory or menu type");
    ErrorCode MENU_COMPONENT_NAME_DUPLICATE = new ErrorCode(1_002_001_006, "Component name already exists");

    // ========== Role module 1-002-002-000 ==========
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1_002_002_000, "Role does not exist");
    ErrorCode ROLE_NAME_DUPLICATE = new ErrorCode(1_002_002_001, "Role name already exists: [{}]");
    ErrorCode ROLE_CODE_DUPLICATE = new ErrorCode(1_002_002_002, "Role code already exists: [{}]");
    ErrorCode ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE = new ErrorCode(1_002_002_003, "System role cannot be modified");
    ErrorCode ROLE_IS_DISABLE = new ErrorCode(1_002_002_004, "Role [{}] is disabled");
    ErrorCode ROLE_ADMIN_CODE_ERROR = new ErrorCode(1_002_002_005, "Role code [{}] is not allowed");

    // ========== User module 1-002-003-000 ==========
    ErrorCode USER_USERNAME_EXISTS = new ErrorCode(1_002_003_000, "Username already exists");
    ErrorCode USER_MOBILE_EXISTS = new ErrorCode(1_002_003_001, "Mobile number already exists");
    ErrorCode USER_EMAIL_EXISTS = new ErrorCode(1_002_003_002, "Email already exists");
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1_002_003_003, "User does not exist");
    ErrorCode USER_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_002_003_004, "Import user list cannot be empty");
    ErrorCode USER_PASSWORD_FAILED = new ErrorCode(1_002_003_005, "User password validation failed");
    ErrorCode USER_IS_DISABLE = new ErrorCode(1_002_003_006, "User [{}] is disabled");
    ErrorCode USER_COUNT_MAX = new ErrorCode(1_002_003_008, "User creation failed: tenant quota exceeded ({})");
    ErrorCode USER_IMPORT_INIT_PASSWORD = new ErrorCode(1_002_003_009, "Initial password cannot be empty");
    ErrorCode USER_MOBILE_NOT_EXISTS = new ErrorCode(1_002_003_010, "Mobile number not registered");
    ErrorCode USER_REGISTER_DISABLED = new ErrorCode(1_002_003_011, "Registration is disabled");

    // ========== Department module 1-002-004-000 ==========
    ErrorCode DEPT_NAME_DUPLICATE = new ErrorCode(1_002_004_000, "Department name already exists");
    ErrorCode DEPT_PARENT_NOT_EXITS = new ErrorCode(1_002_004_001, "Parent department does not exist");
    ErrorCode DEPT_NOT_FOUND = new ErrorCode(1_002_004_002, "Department not found");
    ErrorCode DEPT_EXITS_CHILDREN = new ErrorCode(1_002_004_003, "Cannot delete department with children");
    ErrorCode DEPT_PARENT_ERROR = new ErrorCode(1_002_004_004, "Cannot set itself as parent department");
    ErrorCode DEPT_NOT_ENABLE = new ErrorCode(1_002_004_006, "Department [{}] is disabled");
    ErrorCode DEPT_PARENT_IS_CHILD = new ErrorCode(1_002_004_007, "Cannot set child department as parent");

    // ========== Post module 1-002-005-000 ==========
    ErrorCode POST_NOT_FOUND = new ErrorCode(1_002_005_000, "Post not found");
    ErrorCode POST_NOT_ENABLE = new ErrorCode(1_002_005_001, "Post [{}] is disabled");
    ErrorCode POST_NAME_DUPLICATE = new ErrorCode(1_002_005_002, "Post name already exists");
    ErrorCode POST_CODE_DUPLICATE = new ErrorCode(1_002_005_003, "Post code already exists");

    // ========== Dict type 1-002-006-000 ==========
    ErrorCode DICT_TYPE_NOT_EXISTS = new ErrorCode(1_002_006_001, "Dict type does not exist");
    ErrorCode DICT_TYPE_NOT_ENABLE = new ErrorCode(1_002_006_002, "Dict type is disabled");
    ErrorCode DICT_TYPE_NAME_DUPLICATE = new ErrorCode(1_002_006_003, "Dict type name already exists");
    ErrorCode DICT_TYPE_TYPE_DUPLICATE = new ErrorCode(1_002_006_004, "Dict type already exists");
    ErrorCode DICT_TYPE_HAS_CHILDREN = new ErrorCode(1_002_006_005, "Cannot delete dict type with data");

    // ========== Dict data 1-002-007-000 ==========
    ErrorCode DICT_DATA_NOT_EXISTS = new ErrorCode(1_002_007_001, "Dict data does not exist");
    ErrorCode DICT_DATA_NOT_ENABLE = new ErrorCode(1_002_007_002, "Dict data [{}] is disabled");
    ErrorCode DICT_DATA_VALUE_DUPLICATE = new ErrorCode(1_002_007_003, "Dict value already exists");

    // ========== Notice 1-002-008-000 ==========
    ErrorCode NOTICE_NOT_FOUND = new ErrorCode(1_002_008_001, "Notice not found");

    // ========== SMS channel 1-002-011-000 ==========
    ErrorCode SMS_CHANNEL_NOT_EXISTS = new ErrorCode(1_002_011_000, "SMS channel does not exist");
    ErrorCode SMS_CHANNEL_DISABLE = new ErrorCode(1_002_011_001, "SMS channel is disabled");
    ErrorCode SMS_CHANNEL_HAS_CHILDREN = new ErrorCode(1_002_011_002, "Cannot delete SMS channel with templates");

    // ========== SMS template 1-002-012-000 ==========
    ErrorCode SMS_TEMPLATE_NOT_EXISTS = new ErrorCode(1_002_012_000, "SMS template does not exist");
    ErrorCode SMS_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1_002_012_001, "SMS template code [{}] already exists");
    ErrorCode SMS_TEMPLATE_API_ERROR = new ErrorCode(1_002_012_002, "SMS API call failed: {}");
    ErrorCode SMS_TEMPLATE_API_AUDIT_CHECKING = new ErrorCode(1_002_012_003, "SMS template is under review");
    ErrorCode SMS_TEMPLATE_API_AUDIT_FAIL = new ErrorCode(1_002_012_004, "SMS template rejected: {}");
    ErrorCode SMS_TEMPLATE_API_NOT_FOUND = new ErrorCode(1_002_012_005, "SMS template not found in API");

    // ========== SMS send 1-002-013-000 ==========
    ErrorCode SMS_SEND_MOBILE_NOT_EXISTS = new ErrorCode(1_002_013_000, "Mobile number does not exist");
    ErrorCode SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS = new ErrorCode(1_002_013_001, "Missing template parameter ({})");
    ErrorCode SMS_SEND_TEMPLATE_NOT_EXISTS = new ErrorCode(1_002_013_002, "SMS template does not exist");

    // ========== SMS code 1-002-014-000 ==========
    ErrorCode SMS_CODE_NOT_FOUND = new ErrorCode(1_002_014_000, "Verification code not found");
    ErrorCode SMS_CODE_EXPIRED = new ErrorCode(1_002_014_001, "Verification code expired");
    ErrorCode SMS_CODE_USED = new ErrorCode(1_002_014_002, "Verification code already used");
    ErrorCode SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY = new ErrorCode(1_002_014_004, "Daily SMS limit exceeded");
    ErrorCode SMS_CODE_SEND_TOO_FAST = new ErrorCode(1_002_014_005, "SMS sent too frequently");

    // ========== Tenant 1-002-015-000 ==========
    ErrorCode TENANT_NOT_EXISTS = new ErrorCode(1_002_015_000, "Tenant does not exist");
    ErrorCode TENANT_DISABLE = new ErrorCode(1_002_015_001, "Tenant [{}] is disabled");
    ErrorCode TENANT_EXPIRE = new ErrorCode(1_002_015_002, "Tenant [{}] has expired");
    ErrorCode TENANT_CAN_NOT_UPDATE_SYSTEM = new ErrorCode(1_002_015_003, "System tenant cannot be modified or deleted");
    ErrorCode TENANT_NAME_DUPLICATE = new ErrorCode(1_002_015_004, "Tenant name [{}] already exists");
    ErrorCode TENANT_WEBSITE_DUPLICATE = new ErrorCode(1_002_015_005, "Tenant domain [{}] already exists");

    // ========== Tenant package 1-002-016-000 ==========
    ErrorCode TENANT_PACKAGE_NOT_EXISTS = new ErrorCode(1_002_016_000, "Tenant package does not exist");
    ErrorCode TENANT_PACKAGE_USED = new ErrorCode(1_002_016_001, "Package is in use, cannot delete");
    ErrorCode TENANT_PACKAGE_DISABLE = new ErrorCode(1_002_016_002, "Tenant package [{}] is disabled");
    ErrorCode TENANT_PACKAGE_NAME_DUPLICATE = new ErrorCode(1_002_016_003, "Tenant package name already exists");

    // ========== Social user 1-002-018-000 ==========
    ErrorCode SOCIAL_USER_AUTH_FAILURE = new ErrorCode(1_002_018_000, "Social login failed: {}");
    ErrorCode SOCIAL_USER_NOT_FOUND = new ErrorCode(1_002_018_001, "Social user not found");

    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_PHONE_CODE_ERROR = new ErrorCode(1_002_018_200, "Failed to get phone number");
    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_QRCODE_ERROR = new ErrorCode(1_002_018_201, "Failed to get QR code");
    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_SUBSCRIBE_TEMPLATE_ERROR = new ErrorCode(1_002_018_202, "Failed to get subscription template");
    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_SUBSCRIBE_MESSAGE_ERROR = new ErrorCode(1_002_018_203, "Failed to send subscription message");
    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_ORDER_UPLOAD_SHIPPING_INFO_ERROR = new ErrorCode(1_002_018_204, "Failed to upload shipping info");
    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_ORDER_NOTIFY_CONFIRM_RECEIVE_ERROR = new ErrorCode(1_002_018_205, "Failed to upload order receive info");
    ErrorCode SOCIAL_CLIENT_NOT_EXISTS = new ErrorCode(1_002_018_210, "Social client does not exist");
    ErrorCode SOCIAL_CLIENT_UNIQUE = new ErrorCode(1_002_018_211, "Social client already exists");

    // ========== OAuth2 client 1-002-020-000 ==========
    ErrorCode OAUTH2_CLIENT_NOT_EXISTS = new ErrorCode(1_002_020_000, "OAuth2 client does not exist");
    ErrorCode OAUTH2_CLIENT_EXISTS = new ErrorCode(1_002_020_001, "OAuth2 client already exists");
    ErrorCode OAUTH2_CLIENT_DISABLE = new ErrorCode(1_002_020_002, "OAuth2 client is disabled");
    ErrorCode OAUTH2_CLIENT_AUTHORIZED_GRANT_TYPE_NOT_EXISTS = new ErrorCode(1_002_020_003, "Unsupported grant type");
    ErrorCode OAUTH2_CLIENT_SCOPE_OVER = new ErrorCode(1_002_020_004, "Scope too broad");
    ErrorCode OAUTH2_CLIENT_REDIRECT_URI_NOT_MATCH = new ErrorCode(1_002_020_005, "Invalid redirect_uri: {}");
    ErrorCode OAUTH2_CLIENT_CLIENT_SECRET_ERROR = new ErrorCode(1_002_020_006, "Invalid client_secret: {}");

    // ========== OAuth2 grant 1-002-021-000 ==========
    ErrorCode OAUTH2_GRANT_CLIENT_ID_MISMATCH = new ErrorCode(1_002_021_000, "client_id mismatch");
    ErrorCode OAUTH2_GRANT_REDIRECT_URI_MISMATCH = new ErrorCode(1_002_021_001, "redirect_uri mismatch");
    ErrorCode OAUTH2_GRANT_STATE_MISMATCH = new ErrorCode(1_002_021_002, "state mismatch");

    // ========== OAuth2 code 1-002-022-000 ==========
    ErrorCode OAUTH2_CODE_NOT_EXISTS = new ErrorCode(1_002_022_000, "Code does not exist");
    ErrorCode OAUTH2_CODE_EXPIRE = new ErrorCode(1_002_022_001, "Code expired");

    // ========== Mail account 1-002-023-000 ==========
    ErrorCode MAIL_ACCOUNT_NOT_EXISTS = new ErrorCode(1_002_023_000, "Mail account does not exist");
    ErrorCode MAIL_ACCOUNT_RELATE_TEMPLATE_EXISTS = new ErrorCode(1_002_023_001, "Cannot delete account with templates");

    // ========== Mail template 1-002-024-000 ==========
    ErrorCode MAIL_TEMPLATE_NOT_EXISTS = new ErrorCode(1_002_024_000, "Mail template does not exist");
    ErrorCode MAIL_TEMPLATE_CODE_EXISTS = new ErrorCode(1_002_024_001, "Mail template code [{}] already exists");

    // ========== Mail send 1-002-025-000 ==========
    ErrorCode MAIL_SEND_TEMPLATE_PARAM_MISS = new ErrorCode(1_002_025_000, "Missing template parameter ({})");
    ErrorCode MAIL_SEND_MAIL_NOT_EXISTS = new ErrorCode(1_002_025_001, "Email does not exist");

    // ========== Notify template 1-002-026-000 ==========
    ErrorCode NOTIFY_TEMPLATE_NOT_EXISTS = new ErrorCode(1_002_026_000, "Notification template does not exist");
    ErrorCode NOTIFY_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1_002_026_001, "Notification template code [{}] already exists");

    // ========== Notify send 1-002-028-000 ==========
    ErrorCode NOTIFY_SEND_TEMPLATE_PARAM_MISS = new ErrorCode(1_002_028_000, "Missing template parameter ({})");

}
