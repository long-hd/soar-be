package com.hdl.soar.framework.security.config;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hdl.soar.framework.security.core.filter.TokenAuthenticationFilter;
import com.hdl.soar.framework.web.config.WebProperties;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.DispatcherType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.*;

import static com.hdl.soar.framework.common.util.collection.CollectionUtils.convertList;

/**
 * Custom implementation of Spring Security configuration adapter
 * <br>
 * Purpose: Run before Spring Security auto-configuration to ensure that base packages under org.*
 * still take effect after package refactoring or one-click package renaming.
 */
@AutoConfiguration
@AutoConfigureOrder(-1)
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SoarWebSecurityConfigurerAdapter {

    WebProperties webProperties;
    SecurityProperties securityProperties;

    /**
     * Authentication failure handler Bean
     */
    AuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Insufficient permissions (access denied) handler Bean
     */
    AccessDeniedHandler accessDeniedHandler;

    /**
     * Token authentication filter Bean
     */
    TokenAuthenticationFilter authenticationTokenFilter;

    /**
     * Custom authorization mapping Beans
     *
     * @see #filterChain(HttpSecurity)
     */
    List<AuthorizeRequestsCustomizer> authorizeRequestsCustomizers;

    ApplicationContext applicationContext;

    /**
     * Because Spring Security does not declare the @Bean annotation when creating the AuthenticationManager instance,
     * it cannot be injected properly.
     * By overriding this method in the parent class and adding the @Bean annotation,
     * this issue is resolved.
     */
    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * URL security configuration.
     * <p>
     * anyRequest          | matches all request paths <br>
     * access              | allows access when the Spring EL expression evaluates to true <br>
     * anonymous           | allows access for anonymous users <br>
     * denyAll             | denies access for all users <br>
     * fullyAuthenticated  | allows access only to fully authenticated users
     *                      (not via remember-me automatic login) <br>
     * hasAnyAuthority     | allows access if the user has any of the specified authorities <br>
     * hasAnyRole          | allows access if the user has any of the specified roles <br>
     * hasAuthority        | allows access if the user has the specified authority <br>
     * hasIpAddress        | allows access if the request IP matches the specified IP address <br>
     * hasRole             | allows access if the user has the specified role <br>
     * permitAll           | allows access to everyone <br>
     * rememberMe          | allows access for users authenticated via remember-me <br>
     * authenticated       | allows access for any authenticated user <br>
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // Logout / security configuration
        httpSecurity
                // Enable CORS
                .cors(Customizer.withDefaults())
                // Disable CSRF because we are not using session-based authentication
                .csrf(AbstractHttpConfigurer::disable)
                // Stateless session management (token-based authentication, no HTTP session required)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(c -> c.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // Custom Spring Security handlers
                .exceptionHandling(c -> c
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        // Login / authentication is not handled via Spring Security extension points for now.
        // The main reasons are: supporting multiple user types and multiple login methods would add complexity,
        // and it would also increase the learning cost for users.

        // Retrieve the URL list marked with @PermitAll, which should be accessible without authentication
        Multimap<HttpMethod, String> permitAllUrls = getPermitAllUrlsFromAnnotations();

        // Configure authorization rules for each request
        httpSecurity
                // 1. Global shared rules
                .authorizeHttpRequests(c -> c
                        // 1.1 Static resources, accessible anonymously
                        .requestMatchers(HttpMethod.GET, "/*.html", "/*.css", "/*.js").permitAll()
                        // 1.2 URLs marked with @PermitAll are accessible without authentication
                        .requestMatchers(HttpMethod.GET, permitAllUrls.get(HttpMethod.GET).toArray(new String[0])).permitAll()
                        .requestMatchers(HttpMethod.POST, permitAllUrls.get(HttpMethod.POST).toArray(new String[0])).permitAll()
                        .requestMatchers(HttpMethod.PUT, permitAllUrls.get(HttpMethod.PUT).toArray(new String[0])).permitAll()
                        .requestMatchers(HttpMethod.DELETE, permitAllUrls.get(HttpMethod.DELETE).toArray(new String[0])).permitAll()
                        .requestMatchers(HttpMethod.HEAD, permitAllUrls.get(HttpMethod.HEAD).toArray(new String[0])).permitAll()
                        .requestMatchers(HttpMethod.PATCH, permitAllUrls.get(HttpMethod.PATCH).toArray(new String[0])).permitAll()
                        // 1.3 URLs configured in soar.security.permit-all-urls are also accessible without authentication
                        .requestMatchers(securityProperties.getPermitAllUrls().toArray(new String[0])).permitAll()
                )
                // 2. Custom rules for each module/project
                .authorizeHttpRequests(c -> authorizeRequestsCustomizers
                        .forEach(customizer -> customizer.customize(c)))
                // 3. Default fallback rule: authentication required for all other requests
                .authorizeHttpRequests(c -> c
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll() // Async requests (e.g., SSE) do not require authentication
                        .anyRequest().authenticated());

        // Add Token Filter
        httpSecurity.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    private Multimap<HttpMethod, String> getPermitAllUrlsFromAnnotations() {
        Multimap<HttpMethod, String> result = HashMultimap.create();
        // Get the HandlerMethod collection corresponding to the APIs
        RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping)
                applicationContext.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
        // Find endpoints annotated with @PermitAll
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!handlerMethod.hasMethodAnnotation(PermitAll.class) // Method-level
                    && !handlerMethod.getBeanType().isAnnotationPresent(PermitAll.class)) { // Interface-level
                continue;
            }
            Set<String> urls = new HashSet<>();
            if (entry.getKey().getPatternsCondition() != null) {
                urls.addAll(entry.getKey().getPatternsCondition().getPatterns());
            }
            if (entry.getKey().getPathPatternsCondition() != null) {
                urls.addAll(convertList(entry.getKey().getPathPatternsCondition().getPatterns(), PathPattern::getPatternString));
            }
            if (urls.isEmpty()) {
                continue;
            }

            // Special case: if @RequestMapping is used without specifying the method attribute,
            // it is treated as requiring authentication for all HTTP methods by default
            Set<RequestMethod> methods = entry.getKey().getMethodsCondition().getMethods();
            if (CollUtil.isEmpty(methods)) {
                result.putAll(HttpMethod.GET, urls);
                result.putAll(HttpMethod.POST, urls);
                result.putAll(HttpMethod.PUT, urls);
                result.putAll(HttpMethod.DELETE, urls);
                result.putAll(HttpMethod.HEAD, urls);
                result.putAll(HttpMethod.PATCH, urls);
                continue;
            }
            // Add to result based on the HTTP request method
            entry.getKey().getMethodsCondition().getMethods().forEach(requestMethod -> {
                switch (requestMethod) {
                    case GET:
                        result.putAll(HttpMethod.GET, urls);
                        break;
                    case POST:
                        result.putAll(HttpMethod.POST, urls);
                        break;
                    case PUT:
                        result.putAll(HttpMethod.PUT, urls);
                        break;
                    case DELETE:
                        result.putAll(HttpMethod.DELETE, urls);
                        break;
                    case HEAD:
                        result.putAll(HttpMethod.HEAD, urls);
                        break;
                    case PATCH:
                        result.putAll(HttpMethod.PATCH, urls);
                        break;
                }
            });
        }
        return result;
    }

}
