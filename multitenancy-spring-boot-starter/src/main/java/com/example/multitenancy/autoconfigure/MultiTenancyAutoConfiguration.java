package com.example.multitenancy.autoconfigure;

import com.example.multitenancy.TenantAwareRoutingDataSource;
import com.example.multitenancy.TenantInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Auto-configuration for multi-tenancy.
 */
@Configuration
@EnableConfigurationProperties(MultiTenancyProperties.class)
@ConditionalOnProperty(prefix = "multitenancy", name = "enabled", havingValue = "true")
public class MultiTenancyAutoConfiguration {

    private final MultiTenancyProperties properties;

    public MultiTenancyAutoConfiguration(MultiTenancyProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Map<Object, Object> tenantDataSources() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        
        if (properties.getTenants() == null || properties.getTenants().isEmpty()) {
            throw new IllegalStateException("No tenants configured for multi-tenancy");
        }

        for (MultiTenancyProperties.TenantProperties tenant : properties.getTenants()) {
            DataSource dataSource = DataSourceBuilder.create()
                    .url(tenant.getUrl())
                    .username(tenant.getUsername())
                    .password(tenant.getPassword())
                    .driverClassName(tenant.getDriverClassName())
                    .build();
            targetDataSources.put(tenant.getId(), dataSource);
        }
        return targetDataSources;
    }

    @Bean
    public DataSource dataSource(Map<Object, Object> tenantDataSources) {
        TenantAwareRoutingDataSource routingDataSource = new TenantAwareRoutingDataSource();
        routingDataSource.setTargetDataSources(tenantDataSources);
        
        // Set the first tenant as default if needed
        routingDataSource.setDefaultTargetDataSource(tenantDataSources.values().iterator().next());
        
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }


    @Bean
    public TenantInterceptor tenantInterceptor() {
        java.util.Set<String> tenantIds = properties.getTenants().stream()
                .map(MultiTenancyProperties.TenantProperties::getId)
                .collect(java.util.stream.Collectors.toSet());
        return new TenantInterceptor(tenantIds);
    }

    @Bean
    public WebMvcConfigurer tenantWebMvcConfigurer(TenantInterceptor tenantInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(tenantInterceptor)
                        .addPathPatterns("/api/**");
            }
        };
    }
}
