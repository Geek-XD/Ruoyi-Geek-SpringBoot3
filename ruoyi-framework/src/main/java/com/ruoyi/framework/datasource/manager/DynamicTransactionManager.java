package com.ruoyi.framework.datasource.manager;

import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ruoyi.framework.datasource.DynamicDataSourceContextHolder;

@Component
@EnableTransactionManagement(proxyTargetClass = true)
public class DynamicTransactionManager extends JdbcTransactionManager {

    @Autowired
    DataSourceManager dataSourceManager;

    @Override
    public DataSource getDataSource() {
        DataSource dataSource = dataSourceManager.getDataSource(DynamicDataSourceContextHolder.getDataSourceType());
        if (!Objects.isNull(dataSource)) {
            return dataSource;
        } else {
            return dataSourceManager.getPrimaryDataSource();
        }
    }
}
