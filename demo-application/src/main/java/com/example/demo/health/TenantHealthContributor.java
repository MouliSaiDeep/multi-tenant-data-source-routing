package com.example.demo.health;

import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.NamedContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Component("tenantDbHealthContributor")
public class TenantHealthContributor implements CompositeHealthContributor {

    private final Map<String, HealthContributor> contributors = new LinkedHashMap<>();

    public TenantHealthContributor(@org.springframework.beans.factory.annotation.Qualifier("tenantDataSources") Map<Object, Object> tenantDataSources) {
        for (Map.Entry<Object, Object> entry : tenantDataSources.entrySet()) {
            String tenantId = (String) entry.getKey();
            DataSource dataSource = (DataSource) entry.getValue();
            
            contributors.put(tenantId, (HealthIndicator) () -> {
                try (Connection connection = dataSource.getConnection()) {
                    if (connection.isValid(1)) {
                        return Health.up().build();
                    } else {
                        return Health.down().withDetail("error", "Connection is not valid").build();
                    }
                } catch (Exception e) {
                    return Health.down().withDetail("error", e.getMessage()).build();
                }
            });
        }
    }

    @Override
    public HealthContributor getContributor(String name) {
        return contributors.get(name);
    }

    @Override
    public Iterator<NamedContributor<HealthContributor>> iterator() {
        return contributors.entrySet().stream()
                .map(entry -> NamedContributor.of(entry.getKey(), entry.getValue()))
                .iterator();
    }
}
