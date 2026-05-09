package com.example.multitenancy;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Default implementation of TenantResolver that extracts the tenant ID
 * from the X-Tenant-ID HTTP header.
 */
public class HeaderTenantResolver implements TenantResolver {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public String resolveTenantId(HttpServletRequest request) {
        return request.getHeader(TENANT_HEADER);
    }
}
