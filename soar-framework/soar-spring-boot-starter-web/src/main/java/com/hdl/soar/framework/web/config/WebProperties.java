package com.hdl.soar.framework.web.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

@Data
@Validated
@ConfigurationProperties(prefix = "soar.web")
public class WebProperties {

    @NotNull(message = "APP API cannot be null")
    private Api appApi = new Api("/app-api", "**.controller.app.**");
    @NotNull(message = "Admin API cannot be null")
    private Api adminApi = new Api("/admin-api", "**.controller.admin.**");

    @NotNull(message = "Admin UI cannot be null")
    private Ui adminUi;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Valid
    public static class Api {

        /**
         * API prefix, serving as a unified prefix for all RESTFul APIs provided by controllers.
         *
         * Purpose: Using this prefix helps prevent Swagger or Actuator endpoints from being
         * unintentionally exposed externally via Nginx, which could cause security issues.
         * With this setup, Nginx only needs to forward requests to /api/* endpoints.
         *
         * @see YudaoWebAutoConfiguration#configurePathMatch(PathMatchConfigurer)
         */
        @NotEmpty(message = "API prefix cannot be empty")
        private String prefix;

        /**
         * The Ant-style path pattern of the package containing the controller.
         *
         * Main purpose: to assign the specified {@link #prefix} to the controllers in this package.
         */
        @NotEmpty(message = "Controller package cannot be empty")
        private String controller;

    }

    @Data
    @Valid
    public static class Ui {

        /**
         * Access URL
         */
        private String url;

    }
}
