package com.hdl.soar.module.system.dal.redis;

/**
 * System Redis Key enumeration.
 */
public interface RedisKeyConstants {
    /**
     * Cache for all child department IDs of a given department
     * <ul>
     *  <li>KEY format: dept_children_ids:{id}</li>
     *  <li>VALUE type: String (collection of child department IDs)</li>
     * </ul>
     */
    String DEPT_CHILDREN_ID_LIST = "dept_children_ids";

    /**
     * Role cache
     * <p>
     * KEY format: role:{id} <br>
     * VALUE type: String (role information)
     */
    String ROLE = "role";

    /**
     * Cache of role IDs owned by a user
     * <p>
     * KEY format: user_role_ids:{userId} <br>
     * VALUE type: String (collection of role IDs)
     */
    String USER_ROLE_ID_LIST = "user_role_ids";

    /**
     * Cache of role IDs associated with a specific menu
     * <p>
     * KEY format: menu_role_ids:{menuId} <br>
     * VALUE type: String (collection of role IDs)
     */
    String MENU_ROLE_ID_LIST = "menu_role_ids";

    /**
     * Cache of menu IDs associated with a permission
     * <p>
     * KEY format: permission_menu_ids:{permission} <br>
     * VALUE type: String (array of menu IDs)
     */
    String PERMISSION_MENU_ID_LIST = "permission_menu_ids";

    /**
     * OAuth2 client cache
     * <p>
     * KEY format: oauth_client:{id} <br>
     * VALUE type: String (client information)
     */
    String OAUTH_CLIENT = "oauth_client";

    /**
     * Access token cache
     * <p>
     * KEY format: oauth2_access_token:{token} <br>
     * VALUE type: String (access token info {@link com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO})
     * <p>
     * Uses RedisTemplate due to dynamic expiration time
     */
    String OAUTH2_ACCESS_TOKEN = "oauth2_access_token:%s";

    /**
     * Notification template cache
     * <p>
     * KEY format: notify_template:{code} <br>
     * VALUE type: String (template information)
     */
    String NOTIFY_TEMPLATE = "notify_template";

    /**
     * Mail account cache
     * <p>
     * KEY format: mail_account:{id} <br>
     * VALUE type: String (account information)
     */
    String MAIL_ACCOUNT = "mail_account";

    /**
     * Mail template cache
     * <p>
     * KEY format: mail_template:{code} <br>
     * VALUE type: String (template information)
     */
    String MAIL_TEMPLATE = "mail_template";

    /**
     * SMS template cache
     * <p>
     * KEY format: sms_template:{id} <br>
     * VALUE type: String (template information)
     */
    String SMS_TEMPLATE = "sms_template";

    /**
     * Mini-program subscription template cache
     * <p>
     * KEY format: wxa_subscribe_template:{userType} <br>
     * VALUE type: String (template information)
     */
    String WXA_SUBSCRIBE_TEMPLATE = "wxa_subscribe_template";
}
