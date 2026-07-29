package com.hdl.soar.framework.tenant.core.web;

import cn.hutool.core.util.ObjUtil;
import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.security.core.service.SecurityFrameworkService;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.web.core.util.WebFrameworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * Lets an admin with the {@value #PERMISSION} permission temporarily operate under another
 * tenant's context, by sending a {@code visit-tenant-id} request header.
 *
 * <p>Runs as an interceptor (after {@code TenantContextWebFilter} has set the base tenant),
 * so {@link #preHandle} overrides the tenant for this request and {@link #afterCompletion}
 * restores it. Every tenant-scoped query in the request is then resolved against the visited
 * tenant, since the tenant identifier resolver reads {@link TenantContextHolder}.
 *
 * <p>Security: the visiting admin has FULL access (read and write) to the target tenant.
 * Access is gated solely by the {@value #PERMISSION} permission — grant it only to trusted admins.
 */
@Slf4j
@RequiredArgsConstructor
public class TenantVisitContextInterceptor implements HandlerInterceptor {

    private static final String PERMISSION = "system:tenant:visit";

    private final SecurityFrameworkService securityFrameworkService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long visitTenantId = WebFrameworkUtils.getVisitTenantId(request);
        // No visit header, or visiting the current tenant -> nothing to switch
        if (visitTenantId == null) {
            return true;
        }
        if (ObjUtil.equal(visitTenantId, TenantContextHolder.getTenantId())) {
            return true;
        }
        // Must be a logged-in user
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return true;
        }

        // Must hold the tenant-visit permission
        if (!securityFrameworkService.hasAnyPermissions(PERMISSION)) {
            throw exception0(GlobalErrorCodeConstants.FORBIDDEN.getCode(),
                    "No permission to switch tenant");
        }

        // Switch the tenant for the rest of this request
        loginUser.setVisitTenantId(visitTenantId);
        TenantContextHolder.setTenantId(visitTenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // Restore the admin's own tenant, so nothing downstream keeps the visited tenant
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser != null && loginUser.getTenantId() != null) {
            TenantContextHolder.setTenantId(loginUser.getTenantId());
        }
    }

}
