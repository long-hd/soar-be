package com.hdl.soar.framework.tenant.core.web;

import com.hdl.soar.framework.common.exception.ServiceException;
import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.security.core.service.SecurityFrameworkService;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Verifies the tenant-visit interceptor: it switches the tenant only for a permitted,
 * logged-in admin, and always restores the admin's own tenant afterwards.
 */
class TenantVisitContextInterceptorTest {

    private static final String VISIT_HEADER = "visit-tenant-id";
    private static final Long BASE_TENANT = 1L;
    private static final Long VISIT_TENANT = 2L;

    private SecurityFrameworkService securityFrameworkService;
    private TenantVisitContextInterceptor interceptor;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        securityFrameworkService = mock(SecurityFrameworkService.class);
        interceptor = new TenantVisitContextInterceptor(securityFrameworkService);
        request = mock(HttpServletRequest.class);
        // Base tenant, as TenantContextWebFilter would have set it before the interceptor runs
        TenantContextHolder.setTenantId(BASE_TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    /** Puts a LoginUser into the security context, as an authenticated request would have. */
    private void loginAs(Long tenantId) {
        LoginUser loginUser = LoginUser.builder().id(100L).tenantId(tenantId).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    @Test
    @DisplayName("no visit header -> tenant unchanged")
    void noHeader_noSwitch() {
        when(request.getHeader(VISIT_HEADER)).thenReturn(null);

        assertThat(interceptor.preHandle(request, null, null)).isTrue();
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(BASE_TENANT);
        verifyNoInteractions(securityFrameworkService); // permission never checked
    }

    @Test
    @DisplayName("visiting the current tenant -> no-op")
    void visitSameTenant_noSwitch() {
        when(request.getHeader(VISIT_HEADER)).thenReturn(BASE_TENANT.toString());

        interceptor.preHandle(request, null, null);
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(BASE_TENANT);
        verifyNoInteractions(securityFrameworkService);
    }

    @Test
    @DisplayName("no logged-in user -> no switch, no permission check")
    void noLoginUser_noSwitch() {
        when(request.getHeader(VISIT_HEADER)).thenReturn(VISIT_TENANT.toString());
        // no authentication set

        assertThat(interceptor.preHandle(request, null, null)).isTrue();
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(BASE_TENANT);
        verifyNoInteractions(securityFrameworkService);
    }

    @Test
    @DisplayName("permitted admin -> tenant switched to the visited tenant")
    void withPermission_switches() {
        when(request.getHeader(VISIT_HEADER)).thenReturn(VISIT_TENANT.toString());
        loginAs(BASE_TENANT);
        when(securityFrameworkService.hasAnyPermissions("system:tenant:visit")).thenReturn(true);

        assertThat(interceptor.preHandle(request, null, null)).isTrue();
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(VISIT_TENANT);
    }

    @Test
    @DisplayName("admin without the permission -> forbidden, tenant not switched")
    void withoutPermission_forbidden() {
        when(request.getHeader(VISIT_HEADER)).thenReturn(VISIT_TENANT.toString());
        loginAs(BASE_TENANT);
        when(securityFrameworkService.hasAnyPermissions("system:tenant:visit")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preHandle(request, null, null))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getCode())
                        .isEqualTo(GlobalErrorCodeConstants.FORBIDDEN.getCode()));
        // tenant must not have been switched before the throw
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(BASE_TENANT);
    }

    @Test
    @DisplayName("afterCompletion restores the admin's own tenant")
    void afterCompletion_restores() {
        loginAs(BASE_TENANT);
        TenantContextHolder.setTenantId(VISIT_TENANT); // simulate a switched request

        interceptor.afterCompletion(request, null, null, null);
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(BASE_TENANT);
    }

}
