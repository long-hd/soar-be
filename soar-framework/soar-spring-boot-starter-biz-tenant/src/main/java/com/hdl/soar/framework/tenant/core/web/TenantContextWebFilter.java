package com.hdl.soar.framework.tenant.core.web;

import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.web.core.util.WebFrameworkUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extracts {@code tenant-id} from the HTTP request header and populates
 * {@link TenantContextHolder} for downstream use by Hibernate, services, etc.
 *
 * <p>Runs early in the filter chain (order {@code -104}), before Spring Security
 * and the tenant security filter. This ensures the tenant context is available
 * when {@code TokenAuthenticationFilter} validates the access token.
 *
 * <p>Filter chain order:
 * <pre>
 * CORS → ... → TenantContextWebFilter → ... → Spring Security (TokenAuthFilter) → TenantSecurityWebFilter
 * </pre>
 *
 * @author hdl
 */
public class TenantContextWebFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Parse tenant-id header (returns null if missing or non-numeric)
        Long tenantId = WebFrameworkUtils.getTenantId(request);
        if (tenantId != null) {
            TenantContextHolder.setTenantId(tenantId);
        }
        try {
            filterChain.doFilter(request, response);
        }finally {
            TenantContextHolder.clear();
        }
    }

}
