package com.hdl.soar.framework.security.config;

import com.hdl.soar.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.hdl.soar.framework.common.biz.system.permission.PermissionCommonApi;
import com.hdl.soar.framework.security.core.context.TransmittableThreadLocalSecurityContextHolderStrategy;
import com.hdl.soar.framework.security.core.filter.TokenAuthenticationFilter;
import com.hdl.soar.framework.security.core.handler.AccessDeniedHandlerImpl;
import com.hdl.soar.framework.security.core.handler.AuthenticationEntryPointImpl;
import com.hdl.soar.framework.security.core.service.SecurityFrameworkService;
import com.hdl.soar.framework.security.core.service.SecurityFrameworkServiceImpl;
import com.hdl.soar.framework.web.core.handler.GlobalExceptionHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Spring Security auto-configuration class, mainly used for configuring related components.
 * <p>
 * Note: it cannot be used together with {@link SoarWebSecurityConfigurerAdapter}
 * because it will cause initialization errors.
 * <p>
 * See: https://stackoverflow.com/questions/53847050/spring-boot-delegatebuilder-cannot-be-null-on-autowiring-authenticationmanager
 */
@AutoConfiguration
@AutoConfigureOrder(-1)
public class SoarSecurityAutoConfiguration {

    @Resource
    private SecurityProperties securityProperties;

    /**
     * Bean for handling authentication failures.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {return new AuthenticationEntryPointImpl();}

    /**
     * Bean for handling insufficient permissions.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandlerImpl();
    }

    /**
     * Spring Security password encoder.
     * For security reasons, BCryptPasswordEncoder is used.
     *
     * @see <a href="http://stackabuse.com/password-encoding-with-spring-security/">Password Encoding with Spring Security</a>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(securityProperties.getPasswordEncoderLength());
    }

    /**
     * Bean for the token authentication filter.
     */
    @Bean
    public TokenAuthenticationFilter authenticationTokenFilter(GlobalExceptionHandler globalExceptionHandler,
                                                               OAuth2TokenCommonApi oauth2TokenApi) {
        return new TokenAuthenticationFilter(securityProperties, globalExceptionHandler, oauth2TokenApi);
    }

    @Bean("ss") // Use Spring Security shorthand for convenience
    public SecurityFrameworkService securityFrameworkService(PermissionCommonApi permissionApi) {
        return new SecurityFrameworkServiceImpl(permissionApi);
    }

    /**
     * Declare the use of SecurityContextHolder#setStrategyName(String),
     * configuring TransmittableThreadLocalSecurityContextHolderStrategy as the Security context strategy
     */
    @Bean
    public MethodInvokingFactoryBean securityContextHolderMethodInvokingFactoryBean() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
        methodInvokingFactoryBean.setArguments(TransmittableThreadLocalSecurityContextHolderStrategy.class.getName());
        return methodInvokingFactoryBean;
    }

}
