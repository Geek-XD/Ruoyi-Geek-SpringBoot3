package com.ruoyi.framework.config.properties;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.ruoyi.common.utils.spring.SpringUtils;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource.dynamic")
public class DynamicDataSourceProperties {

    private Map<String, DataSourceProperties> datasource;
    private String primary;

    public Properties build(DataSourceProperties dataSourceProperties) {
        Properties prop = new Properties();
        DruidProperties druidProperties = SpringUtils.getBean(DruidProperties.class);
        prop.setProperty("url", dataSourceProperties.getUrl());
        prop.setProperty("username", dataSourceProperties.getUsername());
        prop.setProperty("password", dataSourceProperties.getPassword());
        prop.setProperty("initialSize", String.valueOf(druidProperties.getInitialSize()));
        prop.setProperty("minIdle", String.valueOf(druidProperties.getMinIdle()));
        prop.setProperty("maxActive", String.valueOf(druidProperties.getMaxActive()));
        prop.setProperty("maxWait", String.valueOf(druidProperties.getMaxWait()));
        prop.setProperty("timeBetweenEvictionRunsMillis",
                String.valueOf(druidProperties.getTimeBetweenEvictionRunsMillis()));
        prop.setProperty("minEvictableIdleTimeMillis", String.valueOf(druidProperties.getMinEvictableIdleTimeMillis()));
        prop.setProperty("maxEvictableIdleTimeMillis", String.valueOf(druidProperties.getMaxEvictableIdleTimeMillis()));
        prop.setProperty("validationQuery", druidProperties.getValidationQuery());
        prop.setProperty("testWhileIdle", String.valueOf(druidProperties.isTestWhileIdle()));
        prop.setProperty("testOnBorrow", String.valueOf(druidProperties.isTestOnBorrow()));
        prop.setProperty("testOnReturn", String.valueOf(druidProperties.isTestOnReturn()));
        // 修改filters配置，确保spring在最前面
        prop.setProperty("filters", SpringUtils.getRequiredProperty("spring.datasource.druid.filters"));
        prop.setProperty("connectionProperties", SpringUtils.getRequiredProperty("spring.datasource.druid.connectionProperties"));
        return prop;
    }

    public void setProperties(DruidDataSource dataSource, Properties prop) {
        dataSource.setUrl(prop.getProperty("url"));
        dataSource.setUsername(prop.getProperty("username"));
        dataSource.setPassword(prop.getProperty("password"));
        if (prop.getProperty("initialSize") != null) {
            dataSource.setInitialSize(Integer.parseInt(prop.getProperty("initialSize")));
        }
        if (prop.getProperty("minIdle") != null) {
            dataSource.setMinIdle(Integer.parseInt(prop.getProperty("minIdle")));
        }
        if (prop.getProperty("maxActive") != null) {
            dataSource.setMaxActive(Integer.parseInt(prop.getProperty("maxActive")));
        }
        if (prop.getProperty("maxWait") != null) {
            dataSource.setMaxWait(Long.parseLong(prop.getProperty("maxWait")));
        }
        if (prop.getProperty("timeBetweenEvictionRunsMillis") != null) {
            dataSource.setTimeBetweenEvictionRunsMillis(
                    Long.parseLong(prop.getProperty("timeBetweenEvictionRunsMillis")));
        }
        if (prop.getProperty("minEvictableIdleTimeMillis") != null) {
            dataSource.setMinEvictableIdleTimeMillis(Long.parseLong(prop.getProperty("minEvictableIdleTimeMillis")));
        }
        if (prop.getProperty("maxEvictableIdleTimeMillis") != null) {
            dataSource.setMaxEvictableIdleTimeMillis(Long.parseLong(prop.getProperty("maxEvictableIdleTimeMillis")));
        }
        if (prop.getProperty("validationQuery") != null) {
            dataSource.setValidationQuery(prop.getProperty("validationQuery"));
        }
        if (prop.getProperty("testWhileIdle") != null) {
            dataSource.setTestWhileIdle(Boolean.parseBoolean(prop.getProperty("testWhileIdle")));
        }
        if (prop.getProperty("testOnBorrow") != null) {
            dataSource.setTestOnBorrow(Boolean.parseBoolean(prop.getProperty("testOnBorrow")));
        }
        if (prop.getProperty("testOnReturn") != null) {
            dataSource.setTestOnReturn(Boolean.parseBoolean(prop.getProperty("testOnReturn")));
        }
        // 确保过滤器配置生效
        try {
            if (prop.getProperty("filters") != null) {
                dataSource.setFilters(prop.getProperty("filters"));
            }
            if (prop.getProperty("connectionProperties") != null) {
                dataSource.setConnectionProperties(prop.getProperty("connectionProperties"));
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

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

}
