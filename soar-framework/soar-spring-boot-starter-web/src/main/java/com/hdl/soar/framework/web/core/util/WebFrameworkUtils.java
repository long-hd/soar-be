package com.hdl.soar.framework.web.core.util;

import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.web.config.WebProperties;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class specifically for the web package.
 */
public class WebFrameworkUtils {

    private static final String REQUEST_ATTRIBUTE_LOGIN_USER_ID = "login_user_id";
    private static final String REQUEST_ATTRIBUTE_LOGIN_USER_TYPE = "login_user_type";

    private static WebProperties properties;

    public WebFrameworkUtils(WebProperties webProperties) {
        WebFrameworkUtils.properties = webProperties;
    }

    /**
     * Gets the current user's ID from the request.
     * Note: This method is only intended for use within the framework!!!
     *
     * @param request the request
     * @return the user ID
     */
    public static Long getLoginUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (Long) request.getAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_ID);
    }

    /**
     * Gets the current user's type.
     * Note: This method is only intended for use by web-related framework components!!!
     *
     * @param request the request
     * @return the user type
     */
    public static Integer getLoginUserType(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // 1. First, get from Attribute
        Integer userType = (Integer) request.getAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_TYPE);
        if (userType != null) {
            return userType;
        }
        // 2. Next, use the convention based on URL prefix
        if (request.getServletPath().startsWith(properties.getAdminApi().getPrefix())) {
            return UserTypeEnum.ADMIN.getValue();
        }
        if (request.getServletPath().startsWith(properties.getAppApi().getPrefix())) {
            return UserTypeEnum.MEMBER.getValue();
        }
        return null;
    }


}
