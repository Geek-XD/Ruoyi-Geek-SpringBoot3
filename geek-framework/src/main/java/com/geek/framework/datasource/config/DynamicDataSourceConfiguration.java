package com.geek.framework.datasource.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geek.framework.datasource.manager.DataSourceManager;
import com.mybatisflex.core.datasource.FlexDataSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceConfiguration {

    private final DataSourceManager dataSourceManager;
    private final DynamicDataSourceProperties dataSourceProperties;

    @Bean
    FlexDataSource dataSource() {
        String primary = dataSourceProperties.getPrimary();
        DataSource primaryDataSource = dataSourceManager.getDataSource(primary);
        FlexDataSource dynamicDataSource = new FlexDataSource(primary, primaryDataSource);
        dataSourceManager.forEachDataSource((name, dataSource) -> {
            dynamicDataSource.addDataSource(name, dataSource);
        });
        return dynamicDataSource;
    }

}
