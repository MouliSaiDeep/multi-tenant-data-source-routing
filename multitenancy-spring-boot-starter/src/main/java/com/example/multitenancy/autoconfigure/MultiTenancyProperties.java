package com.example.multitenancy.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for multi-tenancy.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "multitenancy")
public class MultiTenancyProperties {

    private boolean enabled = false;
    private List<TenantProperties> tenants;

    @Getter
    @Setter
    public static class TenantProperties {
        private String id;
        private String url;
        private String username;
        private String password;
        private String driverClassName = "org.postgresql.Driver";
    }
}
