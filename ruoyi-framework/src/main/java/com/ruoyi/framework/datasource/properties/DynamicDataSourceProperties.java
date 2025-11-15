package com.ruoyi.framework.datasource.properties;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.DruidDataSourceUtils;
import com.ruoyi.common.utils.spring.SpringUtils;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource.dynamic")
public class DynamicDataSourceProperties {

    private Map<String, DataSourceProperties> datasource;
    private String primary;

    public Properties build(DataSourceProperties dataSourceProperties) {
        Properties prop = new Properties();
        DruidProperties druidProperties = SpringUtils.getBean(DruidProperties.class);
        prop.setProperty("druid.url", dataSourceProperties.getUrl());
        prop.setProperty("druid.username", dataSourceProperties.getUsername());
        prop.setProperty("druid.password", dataSourceProperties.getPassword());
        prop.setProperty("druid.initialSize", String.valueOf(druidProperties.getInitialSize()));
        prop.setProperty("druid.minIdle", String.valueOf(druidProperties.getMinIdle()));
        prop.setProperty("druid.maxActive", String.valueOf(druidProperties.getMaxActive()));
        prop.setProperty("druid.maxWait", String.valueOf(druidProperties.getMaxWait()));
        prop.setProperty("druid.validationQuery", druidProperties.getValidationQuery());
        prop.setProperty("druid.testWhileIdle", String.valueOf(druidProperties.isTestWhileIdle()));
        prop.setProperty("druid.testOnBorrow", String.valueOf(druidProperties.isTestOnBorrow()));
        prop.setProperty("druid.testOnReturn", String.valueOf(druidProperties.isTestOnReturn()));
        prop.setProperty("druid.filters", String.valueOf(druidProperties.getFilters()));
        prop.setProperty("druid.connectionProperties", String.valueOf(druidProperties.getConnectionProperties()));
        prop.setProperty("druid.timeBetweenEvictionRunsMillis",
                String.valueOf(druidProperties.getTimeBetweenEvictionRunsMillis()));
        prop.setProperty("druid.minEvictableIdleTimeMillis",
                String.valueOf(druidProperties.getMinEvictableIdleTimeMillis()));
        prop.setProperty("druid.maxEvictableIdleTimeMillis",
                String.valueOf(druidProperties.getMaxEvictableIdleTimeMillis()));
        return prop;
    }

    public void setProperties(DruidDataSource dataSource, Properties prop) {
        DruidDataSourceUtils.configFromProperties(dataSource, prop);
        // 确保过滤器配置生效
        try {
            if (prop.getProperty("druid.connectionProperties") != null) {
                dataSource.setConnectionProperties(prop.getProperty("druid.connectionProperties"));
            }
            // 启用防火墙功能
            dataSource.setProxyFilters(new ArrayList<>());
        } catch (Exception e) {
            throw new RuntimeException("配置Druid过滤器失败", e);
        }
    }

    public Map<String, DataSourceProperties> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, DataSourceProperties> datasource) {
        this.datasource = datasource;
    }

    public String getPrimaryStorageBucket() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

}
