package com.example.multitenancy;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for resolving the tenant identifier from an incoming request.
 */
public interface TenantResolver {

    /**
     * Resolves the tenant ID from the given HTTP request.
     *
     * @param request the HTTP request
     * @return the tenant ID, or null if not found
     */
    String resolveTenantId(HttpServletRequest request);
}
