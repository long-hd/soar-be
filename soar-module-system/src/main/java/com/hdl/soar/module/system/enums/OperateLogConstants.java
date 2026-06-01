package com.hdl.soar.module.system.enums;

/**
 * Operate log constants for the System module.
 * <p>
 * Centralizes module names and SpEL content templates to avoid
 * scattered string literals in service implementations.
 */
public interface OperateLogConstants {

    // ========== User ==========
    String USER_MODULE = "System User";

    String USER_CREATE_CONTENT = "'Created user [' + #reqDTO.nickname + ']'";
    String USER_UPDATE_CONTENT = "'Updated user [' + #reqDTO.nickname + ']'";
    String USER_DELETE_CONTENT = "'Deleted user ID [' + #id + ']'";
    String USER_RESET_PWD_CONTENT = "'Reset password for user ID [' + #id + ']'";

    // ========== Role ==========
    String ROLE_MODULE = "System Role";

    String ROLE_CREATE_CONTENT = "'Created role [' + #reqDTO.name + ']'";
    String ROLE_UPDATE_CONTENT = "'Updated role [' + #reqDTO.name + ']'";
    String ROLE_DELETE_CONTENT = "'Deleted role ID [' + #id + ']'";

}
