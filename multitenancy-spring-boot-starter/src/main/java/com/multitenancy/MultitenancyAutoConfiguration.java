package com.multitenancy;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@EnableConfigurationProperties(MultiTenancyProperties.class)
@ConditionalOnProperty(prefix = "multitenancy", name = "enabled", havingValue = "true")
public class MultitenancyAutoConfiguration implements WebMvcConfigurer {

    private final MultiTenancyProperties properties;

    public MultitenancyAutoConfiguration(MultiTenancyProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor(properties));
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        
        for (MultiTenancyProperties.TenantProperties tenant : properties.getTenants()) {
            DataSource ds = DataSourceBuilder.create()
                    .url(tenant.getUrl())
                    .username(tenant.getUsername())
                    .password(tenant.getPassword())
                    .build();
            targetDataSources.put(tenant.getId(), ds);
        }

        TenantAwareRoutingDataSource routingDataSource = new TenantAwareRoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        
        // Set a default data source to avoid startup errors when no tenant is selected
        if (!targetDataSources.isEmpty()) {
            routingDataSource.setDefaultTargetDataSource(targetDataSources.values().iterator().next());
        }
        
        routingDataSource.afterPropertiesSet();
        
        return routingDataSource;
    }
}
