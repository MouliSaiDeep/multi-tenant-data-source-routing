package com.example.demo.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Component("tenantDbHealthIndicator")
public class TenantHealthIndicator implements HealthIndicator {

    private final Map<Object, Object> tenantDataSources;

    public TenantHealthIndicator(Map<Object, Object> tenantDataSources) {
        this.tenantDataSources = tenantDataSources;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean allUp = true;

        for (Map.Entry<Object, Object> entry : tenantDataSources.entrySet()) {
            String tenantId = (String) entry.getKey();
            DataSource dataSource = (DataSource) entry.getValue();

            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(1)) {
                    details.put(tenantId, Map.of("status", "UP"));
                } else {
                    details.put(tenantId, Map.of("status", "DOWN"));
                    allUp = false;
                }
            } catch (Exception e) {
                details.put(tenantId, Map.of("status", "DOWN", "error", e.getMessage()));
                allUp = false;
            }
        }

        return Health.status(allUp ? Status.UP : Status.DOWN)
                .withDetails(details)
                .build();
    }
}

