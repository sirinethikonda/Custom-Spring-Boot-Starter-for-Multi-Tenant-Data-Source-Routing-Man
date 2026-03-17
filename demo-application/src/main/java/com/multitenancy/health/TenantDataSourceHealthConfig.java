package com.multitenancy.health;

import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class TenantDataSourceHealthConfig {

    @Bean
    public HealthContributor datasourcesHealthContributor(Map<String, DataSource> dataSources) {
        return CompositeHealthContributor.fromMap(
            dataSources.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("dataSource")) // Filter out the routing datasource itself
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> (HealthIndicator) () -> {
                        try {
                            new JdbcTemplate(entry.getValue()).execute("SELECT 1");
                            return Health.up().build();
                        } catch (Exception e) {
                            return Health.down(e).withDetail("error", e.getMessage()).build();
                        }
                    }
                ))
        );
    }
}
