package com.hdl.soar.framework.tenant.core.security;

import cn.hutool.core.collection.CollUtil;
import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.util.servlet.ServletUtils;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import com.hdl.soar.framework.tenant.config.TenantProperties;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.tenant.core.service.TenantFrameworkService;
import com.hdl.soar.framework.web.config.WebProperties;
import com.hdl.soar.framework.web.core.filter.ApiRequestFilter;
import com.hdl.soar.framework.web.core.handler.GlobalExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Tenant security filter — enforces tenant isolation at the web layer.
 *
 * <p>Runs AFTER Spring Security (order {@code -99}), so {@link LoginUser}
 * is already available in {@code SecurityContextHolder}.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>If user is logged in: verify user's tenantId matches the request's tenantId
 *       (prevents cross-tenant access)</li>
 *   <li>If request is NOT an ignored URL and no tenantId: reject</li>
 *   <li>If request IS an ignored URL and no tenantId: set {@code ignore=true}
 *       so Hibernate skips tenant filtering</li>
 * </ol>
 *
 * <p>Only applies to API paths ({@code /admin-api/**}, {@code /app-api/**})
 * via {@link ApiRequestFilter}.
 */
@Slf4j
public class TenantSecurityWebFilter extends ApiRequestFilter {

    private final TenantProperties tenantProperties;

    /**
     * URLs that allow missing tenant header.
     * Combines {@link TenantProperties#getIgnoreUrls()} with URLs from
     * controllers annotated with {@code @TenantIgnore}.
     */
    private final Set<String> ignoreUrls;
    private final TenantFrameworkService tenantFrameworkService;
    private final GlobalExceptionHandler globalExceptionHandler;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public TenantSecurityWebFilter(WebProperties webProperties,
                                   TenantProperties tenantProperties,
                                   Set<String> ignoreUrls,
                                   GlobalExceptionHandler globalExceptionHandler,
                                   TenantFrameworkService tenantFrameworkService) {
        super(webProperties);
        this.tenantProperties = tenantProperties;
        this.ignoreUrls = ignoreUrls;
        this.tenantFrameworkService = tenantFrameworkService;
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Long tenantId = TenantContextHolder.getTenantId();

        // === 1. Logged-in user: verify or populate tenant context ===
        LoginUser user = SecurityFrameworkUtils.getLoginUser();
        if(user != null) {
            if (tenantId == null) {
                // No tenant header — use the user's own tenantId
                tenantId = user.getTenantId();
                TenantContextHolder.setTenantId(tenantId);
            } else if (!Objects.equals(user.getTenantId(), tenantId)) {
                // Tenant header doesn't match user's tenant — cross-tenant attempt
                log.error("[doFilterInternal][Tenant({}) User({}/{}) attempted cross-tenant access to Tenant({}) URL({} {})]",
                        user.getTenantId(), user.getId(), user.getUserType(),
                        tenantId, request.getRequestURI(), request.getMethod());
                ServletUtils.writeJSON(response, CommonResult.error(
                        GlobalErrorCodeConstants.FORBIDDEN.getCode(),
                        "No permission to access this tenant's data"));
                return;
            }
        }

        // === 2. Check if this URL requires a tenant header ===
        if(!isIgnoreUrl(request)) {
            // 2.1 Not an ignored URL — tenant header is mandatory
            if (tenantId == null) {
                log.error("[doFilterInternal][URL({} {}) missing tenant-id header]",
                        request.getRequestURI(), request.getMethod());
                ServletUtils.writeJSON(response, CommonResult.error(
                        GlobalErrorCodeConstants.BAD_REQUEST.getCode(),
                        "Missing tenant-id request header"));
                return;
            }
            // 2.2 Validate tenant is active/not expired via TenantFrameworkService
            try {
                tenantFrameworkService.validTenant(tenantId);
            } catch (Throwable ex) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        } else {
            // Ignored URL without tenant header — set ignore so Hibernate skips filtering
            if (tenantId == null) {
                TenantContextHolder.setIgnore(true);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isIgnoreUrl(HttpServletRequest request) {
        String apiUri = request.getRequestURI().substring(request.getContextPath().length());

        // Fast exact match
        if (CollUtil.contains(tenantProperties.getIgnoreUrls(), apiUri)
                || CollUtil.contains(ignoreUrls, apiUri)) {
            return true;
        }

        // Ant-style pattern match (e.g. /open-api/**)
        for (String url : tenantProperties.getIgnoreUrls()) {
            if (pathMatcher.match(url, apiUri)) {
                return true;
            }
        }
        for (String url : ignoreUrls) {
            if (pathMatcher.match(url, apiUri)) {
                return true;
            }
        }
        return false;
    }
}
