package com.hdl.soar.module.system.controller.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin Backend - Permission Information Response DTO for the Logged-in User, including additional user information and role list")
public class AuthPermissionInfoRespDTO {

    @Schema(description = "User information", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserDTO user;

    @Schema(description = "Role identifier array", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<String> roles;

    @Schema(description = "Operation permission array", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<String> permissions;

    @Schema(description = "Menu tree", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MenuDTO> menus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User Information DTO")
    public static class UserDTO {

        @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "User nickname", requiredMode = Schema.RequiredMode.REQUIRED, example = "Soar Source Code")
        private String nickname;

        @Schema(description = "User avatar", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.jpg")
        private String avatar;

        @Schema(description = "Department ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
        private Long deptId;

        @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED, example = "Alice")
        private String username;

        @Schema(description = "User email", example = "alice@gmail.com")
        private String email;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Admin Backend - Logged-in User Menu Information Response DTO")
    public static class MenuDTO {

        @Schema(description = "Menu ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "Parent Menu ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long parentId;

        @Schema(description = "Menu name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Abc management")
        private String name;

        @Schema(description = "Tab dispatcher key", example = "system-user")
        private String tabKey;

        @Schema(description = "Route path, required only when the menu type is Menu or Directory", example = "post")
        private String path;

        @Schema(description = "Component path, required only when the menu type is Menu", example = "system/post/index")
        private String component;

        @Schema(description = "Component name", example = "SystemUser")
        private String componentName;

        @Schema(description = "Menu icon, required only when the menu type is Menu or Directory", example = "/menu/list")
        private String icon;

        @Schema(description = "Whether visible", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
        private Boolean visible;

        @Schema(description = "Whether cache is enabled", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
        private Boolean keepAlive;

        @Schema(description = "Whether always displayed", example = "false")
        private Boolean alwaysShow;

        /**
         * Child routes
         */
        private List<MenuDTO> children;

    }

}
