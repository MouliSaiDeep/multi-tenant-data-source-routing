package com.example.multitenancy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Interceptor that extracts the X-Tenant-ID header from incoming requests
 * and sets it in the TenantContext.
 */
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantResolver tenantResolver;
    private final Set<String> allowedTenants;

    public TenantInterceptor(TenantResolver tenantResolver, Set<String> allowedTenants) {
        this.tenantResolver = tenantResolver;
        this.allowedTenants = allowedTenants;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tenantId = tenantResolver.resolveTenantId(request);
        
        if (tenantId == null || tenantId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Bad Request\", \"message\": \"X-Tenant-ID header is missing\"}");
            return false;
        }

        if (!allowedTenants.contains(tenantId)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            String safeTenantId = tenantId.replace("\\", "\\\\").replace("\"", "\\\"");
            response.getWriter().write("{\"error\": \"Not Found\", \"message\": \"Tenant not found: " + safeTenantId + "\"}");
            return false;
        }

        TenantContext.setCurrentTenant(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}

