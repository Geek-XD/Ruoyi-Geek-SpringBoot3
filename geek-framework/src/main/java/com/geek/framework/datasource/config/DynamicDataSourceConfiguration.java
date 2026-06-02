package com.geek.framework.datasource.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geek.framework.datasource.manager.DataSourceManager;
import com.geek.framework.datasource.properties.DynamicDataSourceProperties;
import com.mybatisflex.core.datasource.FlexDataSource;

@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceConfiguration {

    @Autowired
    private DynamicDataSourceProperties dataSourceProperties;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Bean
    public FlexDataSource dataSource() {
        String primary = dataSourceProperties.getPrimary();
        DataSource primaryDataSource = dataSourceManager.getDataSource(primary);
        FlexDataSource dynamicDataSource = new FlexDataSource(primary, primaryDataSource);
        dataSourceManager.forEachDataSource((name, dataSource) -> {
            dynamicDataSource.addDataSource(name, dataSource);
        });
        return dynamicDataSource;
    }

}
