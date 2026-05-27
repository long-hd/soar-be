package com.hdl.soar.framework.swagger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.SecurityService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hdl.soar.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * Swagger auto-configuration class based on OpenAPI + Springdoc.
 * <p>
 * Friendly Tips:
 * <ul>
 * <li>1. Springdoc documentation:
 *    <a href="https://github.com/springdoc/springdoc-openapi">Repository</a></li>
 * <li>2. The Swagger specification was renamed to the OpenAPI specification in 2015,
 *    but they are essentially the same thing.</li>
 * </ul>
 * @author hdl
 */
@AutoConfiguration
@ConditionalOnClass({OpenAPI.class})
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(prefix = "springdoc.api-docs", name = "enabled", havingValue = "true", matchIfMissing = true) // Disable when set to false
public class SoarSwaggerAutoConfiguration {

    // ========== Global OpenAPI Configuration ==========

    @Bean
    public OpenAPI createApi(SwaggerProperties properties) {
        Map<String, SecurityScheme> securitySchemas = buildSecuritySchemes();

        OpenAPI openAPI = new OpenAPI()
                // API information
                .info(buildInfo(properties))
                // API security configuration
                .components(new Components().securitySchemes(securitySchemas))
                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));

        securitySchemas.keySet()
                .forEach(key -> openAPI.addSecurityItem(new SecurityRequirement().addList(key)));

        return openAPI;
    }

    /**
     * API summary information
     */
    private Info buildInfo(SwaggerProperties properties) {
        return new Info()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .version(properties.getVersion())
                .contact(new Contact()
                        .name(properties.getAuthor())
                        .url(properties.getUrl())
                        .email(properties.getEmail()))
                .license(new License()
                        .name(properties.getLicense())
                        .url(properties.getLicenseUrl()));
    }

    /**
     * Security mode: configure token parameter passed via request header Authorization
     */
    private Map<String, SecurityScheme> buildSecuritySchemes() {
        Map<String, SecurityScheme> securitySchemes = new HashMap<>();

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // Type
                .name(HttpHeaders.AUTHORIZATION) // Header name
                .in(SecurityScheme.In.HEADER); // Token location

        securitySchemes.put(HttpHeaders.AUTHORIZATION, securityScheme);

        return securitySchemes;
    }

    /**
     * Custom OpenAPI handler
     */
    @Bean
    @Primary // Purpose: Use the OpenAPIService Bean we created as the primary one to avoid startup errors after one-click package refactoring!
    public OpenAPIService openApiBuilder(Optional<OpenAPI> openAPI,
                                         SecurityService securityParser,
                                         SpringDocConfigProperties springDocConfigProperties,
                                         PropertyResolverUtils propertyResolverUtils,
                                         Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomizers,
                                         Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomizers,
                                         Optional<JavadocProvider> javadocProvider) {
        return new OpenAPIService(openAPI, securityParser, springDocConfigProperties,
                propertyResolverUtils, openApiBuilderCustomizers, serverBaseUrlCustomizers, javadocProvider);
    }

    // ========== Grouped OpenAPI Configuration ==========

    /**
     * API group for all modules
     */
    @Bean
    public GroupedOpenApi allGroupedOpenApi() {
        return buildGroupedOpenApi("all", "");
    }

    public static GroupedOpenApi buildGroupedOpenApi(String group) {
        return buildGroupedOpenApi(group, group);
    }

    public static GroupedOpenApi buildGroupedOpenApi(String group, String path) {
        return GroupedOpenApi.builder()
                .group(group)
                .pathsToMatch("/admin-api/" + path + "/**", "/app-api/" + path + "/**")
                .addOperationCustomizer((operation, handlerMethod) -> operation
                        .addParametersItem(buildTenantHeaderParameter()))
                .addOperationCustomizer(buildOperationIdCustomizer())
                .build();
    }

    /**
     * Build tenant ID request header parameter
     *
     * @return multi-tenant parameter
     */
    private static Parameter buildTenantHeaderParameter() {
        return new Parameter()
                .name(HEADER_TENANT_ID) // header name
                .description("Tenant ID") // description
                .in(String.valueOf(SecurityScheme.In.HEADER)) // request header
                .schema(new IntegerSchema()
                        ._default(1L)
                        .name(HEADER_TENANT_ID)
                        .description("Tenant ID")); // default: tenant ID = 1
    }


    /**
     * Core: custom OperationId generation rule combining "class prefix + method name"
     *
     * @see <a href="https://github.com/YunaiV/ruoyi-vue-pro/issues/957">app-api prefix not effective, always using admin-api</a>
     */
    private static OperationCustomizer buildOperationIdCustomizer() {
        return (operation, handlerMethod) -> {
            // 1. Get controller class name (e.g. UserController)
            String className = handlerMethod.getBeanType().getSimpleName();

            // 2. Extract class prefix (remove "Controller" suffix, e.g. UserController -> User)
            String classPrefix = className.replaceAll("Controller$", "");

            // 3. Get method name (e.g. list)
            String methodName = handlerMethod.getMethod().getName();

            // 4. Build operationId (e.g. User_list)
            String operationId = classPrefix + "_" + methodName;

            // 5. Set custom operationId
            operation.setOperationId(operationId);

            return operation;
        };
    }

}
