package com.geek.framework.datasource.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;
import com.geek.framework.datasource.config.DynamicDataSourceProperties;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@DependsOn("liquibase")
@RequiredArgsConstructor
public class DataSourceManager implements InitializingBean {

    private final DynamicDataSourceProperties dataSourceProperties;

    private Map<String, DataSource> targetDataSources = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        dataSourceProperties.getDatasource().forEach((name, props) -> {
            long start = System.currentTimeMillis();
            Properties properties = dataSourceProperties.build(props);
            DruidDataSource dataSource = createDataSource(name, properties);
            log.info("数据源：{} 链接成功，耗时：{}ms", name, System.currentTimeMillis() - start);
            targetDataSources.put(name, dataSource);
        });
    }

    public DruidDataSource createDataSource(String name, Properties prop) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setConnectProperties(prop);
        dataSource.setConnectionProperties(prop.getProperty("druid.connectionProperties"));
        try (Connection conn = dataSource.getConnection()) {
            dataSource.validateConnection(conn);
            return dataSource;
        } catch (SQLException e) {
            throw new RuntimeException("数据源连接验证失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        forEachDataSource((name, dataSource) -> {
            if (dataSource instanceof DruidDataSource) {
                ((DruidDataSource) dataSource).close();
            } else {
                log.warn("数据源：{} 不是 DruidDataSource，无法关闭", name);
            }
        });
    }

    public DataSource getDataSource(String name) {
        return targetDataSources.get(name);
    }

    public DataSource getPrimaryDataSource() {
        return targetDataSources.get(dataSourceProperties.getPrimary());
    }

    public void forEachDataSource(BiConsumer<String, DataSource> consumer) {
        targetDataSources.forEach(consumer);
    }
}
